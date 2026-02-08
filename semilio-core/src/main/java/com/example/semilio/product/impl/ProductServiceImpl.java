package com.example.semilio.product.impl;

import com.example.semilio.category.Category;
import com.example.semilio.category.CategoryRepository;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.favorite.FavoriteRepository;
import com.example.semilio.image.Image;
import com.example.semilio.image.ImageService;
import com.example.semilio.product.*;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.model.ProductStats;
import com.example.semilio.product.request.ProductRequestDTO;
import com.example.semilio.product.request.ProductSearchCriteriaRequest;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.response.ProductDetailDTO;
import com.example.semilio.product.response.ProductSummaryDTO;
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
    private final ImageService imageService;
    private final SecurityService securityService;
    private final FavoriteRepository favoriteRepository;

    private final String S3_FOLDER_NAME = "products";

    @Override
    public ProductCardResponse createProduct(ProductRequestDTO dto,
                                             List<MultipartFile> images,
                                             Authentication principal) {
        User currentUser = securityService.getCurrentUser(principal);

        var category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ERROR));

        var product = mapper.toEntity(dto);
        product.setSeller(currentUser);
        product.setCategory(category);
        product.setStats(new ProductStats(0, 0, 0));

        List<Image> saveImage = imageService.createImages(images, "products" );
        product.setImages(saveImage);

        var saved = productRepository.save(product);
        var summaryDTO = mapper.toProductCardResponse(saved);

        log.info("Created product: id={}, title='{}', sellerId={}",
                saved.getId(), saved.getTitle(), saved.getSeller().getId());

        return summaryDTO;
    }

    @Override
    public ProductSummaryDTO updateProduct(String productId,
                                           ProductRequestDTO dto,
                                           List<MultipartFile> newFiles,
                                           Authentication principal) {

        var existing = getProduct(productId);
        User currentUser = securityService.getCurrentUser(principal);

        if (!existing.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        updateProductFields(existing, dto);

        List<Image> updatedImages = imageService.updateImages(
                existing.getImages(),
                dto.getRemainingImages(),
                newFiles,
                "products"
        );

        existing.getImages().clear();


        existing.getImages().addAll(updatedImages);

        var saved = productRepository.save(existing);
        log.info("Updated product: id={}, title='{}'", saved.getId(), saved.getTitle());

        return mapper.toSummaryDTO(saved);
    }

    private void updateProductFields(Product existing, ProductRequestDTO dto) {
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            existing.setCategory(category);
        }
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        if (existing.getPrice() != null) {
            existing.getPrice().setAmount(dto.getAmount());
        }
        existing.setSize(dto.getSize());
        existing.setCondition(dto.getCondition());
        existing.setBrand(dto.getBrand());
        existing.setColor(dto.getColor());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(String productId) {
        var product = getProduct(productId);
        return mapper.toDetailDTO(product);
    }

    @Override
    @Transactional
    public void deleteProduct(String productId, Authentication principal) {
        var existing = getProduct(productId);

        String currentUserId = securityService.getCurrentUserId(principal);

        boolean isOwner = existing.getSeller().getId().equals(currentUserId);

        if (!isOwner) {
            log.warn("SECURITY ALERT: User {} tried to delete product {} owned by seller {}",
                    currentUserId, productId, existing.getSeller().getId());
            throw new BusinessException(ErrorCode.FORBIDDEN_ACTION);
        }

        try {
            imageService.deleteImages(existing.getImages());
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
    public void changeVisibility(String productId, Authentication principal) {
        var product = getProduct(productId);

        String currentUserId = securityService.getCurrentUserId(principal);

        if (!product.getSeller().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACTION);
        }

        if (product.getStatus() == Status.ACTIVE) {
            product.setStatus(Status.HIDDEN);
        } else if (product.getStatus() == Status.HIDDEN) {
            product.setStatus(Status.ACTIVE);
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
        Page<Product> products = productRepository.findAllBySellerIdAndStatus(sellerId, Status.ACTIVE, pageable);

        return enrichWithLikes(products, principal);
    }

    @Async
    public void addViewAsync(String productId) {
        productRepository.incrementViews(productId);
    }

    @Override
    public ProductDetailDTO getProductBySlug(String slug) {
        var product = productRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return mapper.toDetailDTO(product);
    }

    private Product getProduct(String productId) {
        return  productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Page<ProductCardResponse> enrichWithLikes(Page<Product> productsPage, Authentication principal) {
        Set<String> likedProductIds = new HashSet<>();

        if (principal != null && !productsPage.isEmpty()) {
            String userId = securityService.getCurrentUserId(principal);

            List<String> visibleIds = productsPage.getContent().stream()
                    .map(Product::getId)
                    .toList();

            likedProductIds = favoriteRepository.findLikedProductIds(userId, visibleIds);
        }

        Set<String> finalLikedIds = likedProductIds;

        return productsPage.map(product ->
                mapper.toProductCardResponse(product, finalLikedIds.contains(product.getId()))
        );
    }
}
