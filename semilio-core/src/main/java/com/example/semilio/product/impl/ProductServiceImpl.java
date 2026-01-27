package com.example.semilio.product.impl;

import com.example.semilio.category.Category;
import com.example.semilio.category.CategoryRepository;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.favorite.FavoriteRepository;
import com.example.semilio.product.*;
import com.example.semilio.product.request.ProductRequestDTO;
import com.example.semilio.product.request.ProductSearchCriteriaRequest;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.response.ProductDetailDTO;
import com.example.semilio.product.response.ProductSummaryDTO;
import com.example.semilio.service.S3Service;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper mapper;
    private final S3Service s3Service;
    private final SecurityService securityService;
    private final FavoriteRepository favoriteRepository;

    @Override
    public ProductCardResponse createProduct(ProductRequestDTO dto, List<MultipartFile> images, Authentication principal) {
        User currentUser = securityService.getCurrentUser(principal);

        var category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERROR));

        var product = mapper.toEntity(dto);
        product.setSeller(currentUser);
        product.setCategory(category);
        product.setStats(new ProductStats(0, 0, 0));

        if (images != null && !images.isEmpty()) {
            var imageUrls = s3Service.uploadImages(images, "products/" + currentUser.getId());
            product.setImageUrls(imageUrls);
            product.setMainImageUrl(imageUrls.get(0));
        }

        var saved = productRepository.save(product);
        var summaryDTO = mapper.toProductCardResponse(saved);

        log.info("Created product: id={}, title='{}', sellerId={}",
                saved.getId(), saved.getTitle(), saved.getSeller().getId());

        return summaryDTO;
    }

    @Override
    public ProductSummaryDTO updateProduct(Long productId, ProductRequestDTO dto) {
        var existing = getProduct(productId);

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ERROR));
            existing.setCategory(category);
        }

        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setSize(dto.getSize());
        existing.setCondition(dto.getCondition());

        var updated = productRepository.save(existing);
        var summaryDTO = mapper.toSummaryDTO(updated);

        log.info("Updated product: id={}, title='{}'", updated.getId(), updated.getTitle());

        return summaryDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(Long productId) {
        var product = getProduct(productId);
        return mapper.toDetailDTO(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Authentication principal) {
        var existing = getProduct(productId);

        String currentUserId = securityService.getCurrentUserId(principal);

        boolean isOwner = existing.getSeller().getId().equals(currentUserId);

        if (!isOwner) {
            log.warn("SECURITY ALERT: User {} tried to delete product {} owned by seller {}",
                    currentUserId, productId, existing.getSeller().getId());
            throw new BusinessException(ErrorCode.FORBIDDEN_ACTION);
        }

        try {
            existing.getImageUrls().forEach(s3Service::deleteFile);
        } catch (Exception e) {
            log.error("Failed to clean up S3 files for product {}: {}", productId, e.getMessage());
        }

        productRepository.delete(existing);

        log.info("Product successfully deleted: id={}, byUser={}",
                productId, currentUserId);
    }

    @Override
    public Page<ProductCardResponse> getUserProducts(Authentication principal, Pageable pageable) {
        String currentUserId = this.securityService.getCurrentUserId(principal);

        Page<Product> productsPage = this.productRepository
                .findAllBySeller_Id(currentUserId, pageable);

        return productsPage.map(mapper::toProductCardResponse);
    }

    @Override
    @Transactional
    public void changeVisibility(Long productId, Authentication principal) {
        var product = getProduct(productId);

        String currentUserId = securityService.getCurrentUserId(principal);

        if (!product.getSeller().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACTION);
        }

        if (product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.HIDDEN);
        } else if (product.getStatus() == ProductStatus.HIDDEN) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        productRepository.save(product);

        log.info("Product visibility changed: id={}, newStatus={}", productId, product.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCardResponse> getFeaturedProducts(Authentication principal, String seed, Pageable pageable) {
        Page<Product> productsPage = productRepository.findFeaturedProducts(seed, pageable);

        return enrichWithLikes(productsPage, principal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCardResponse> search(ProductSearchCriteriaRequest criteria, Pageable pageable, Authentication principal) {
        log.info("Search request received: criteria={}, pageable={}", criteria, pageable);

        Specification<Product> spec = ProductSpecifications.filterBy(criteria);
        Page<Product> products = productRepository.findAll(spec, pageable);

        return enrichWithLikes(products, principal);
    }

    @Override
    public Page<ProductCardResponse> getSellerProducts(Authentication principal, String sellerId, Pageable pageable) {
        Page<Product> products = productRepository.findAllBySellerIdAndStatus(sellerId, ProductStatus.ACTIVE, pageable);

        return enrichWithLikes(products, principal);
    }

    @Async
    public void addViewAsync(Long productId) {
        productRepository.incrementViews(productId);
    }

    private Product getProduct(Long productId) {
        return  productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Page<ProductCardResponse> enrichWithLikes(Page<Product> productsPage, Authentication principal) {
        Set<Long> likedProductIds = new HashSet<>();

        if (principal != null && !productsPage.isEmpty()) {
            String userId = securityService.getCurrentUserId(principal);

            List<Long> visibleIds = productsPage.getContent().stream()
                    .map(Product::getId)
                    .toList();

            likedProductIds = favoriteRepository.findLikedProductIds(userId, visibleIds);
        }

        Set<Long> finalLikedIds = likedProductIds;

        return productsPage.map(product ->
                mapper.toProductCardResponse(product, finalLikedIds.contains(product.getId()))
        );
    }
}
