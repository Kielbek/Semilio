package com.example.semilio.product.service.impl;

import com.example.semilio.category.repository.CategoryRepository;
import com.example.semilio.comon.SlugUtils;
import com.example.semilio.dictionary.repository.BrandRepository;
import com.example.semilio.dictionary.repository.ColorRepository;
import com.example.semilio.dictionary.repository.SizeRepository;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.favorite.repository.FavoriteRepository;
import com.example.semilio.image.model.Image;
import com.example.semilio.image.service.ImageService;
import com.example.semilio.product.mapper.ProductMapper;
import com.example.semilio.product.model.Price;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.model.ProductStats;
import com.example.semilio.product.repository.ProductRepository;
import com.example.semilio.product.request.ProductRequest;
import com.example.semilio.product.request.ProductSearchCriteriaRequest;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.response.ProductDetailDTO;
import com.example.semilio.product.service.ProductService;
import com.example.semilio.product.enums.Status;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final FavoriteRepository favoriteRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ProductMapper mapper;
    private final ImageService imageService;
    private final SecurityService securityService;
    private final ProductCardEnricher productCardEnricher;

    @Override
    public ProductCardResponse createProduct(ProductRequest dto,
                                             List<MultipartFile> images,
                                             Authentication principal) {

        User currentUser = securityService.getCurrentUser(principal);

        var product = mapper.toEntity(dto);
        product.setSeller(currentUser);
        product.setCategory(categoryRepository.getReferenceById(dto.categoryId()));
        product.setBrand(brandRepository.getReferenceById(dto.brandId()));
        product.setColor(colorRepository.getReferenceById(dto.colorId()));
        product.setSize(sizeRepository.getReferenceById(dto.sizeId()));
        product.setStats(new ProductStats(0, 0, 0));

        List<Image> saveImage = imageService.createImages(images, "products" );
        product.setImages(saveImage);

        var saved = productRepository.save(product);
        var summaryDTO = mapper.toProductCardResponse(saved, product.getMainImage());

        log.info("Created product: id={}, title='{}', sellerId={}",
                saved.getId(), saved.getTitle(), saved.getSeller().getId());

        return summaryDTO;
    }

    @Override
    public ProductCardResponse updateProduct(UUID productId,
                                             ProductRequest dto,
                                             List<MultipartFile> newFiles,
                                             Authentication principal) {

        var existing = this.productRepository.findByIdWithoutSeller(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        User currentUser = securityService.getCurrentUser(principal);

        if (!existing.getSeller().getId().equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        updateProductFields(existing, dto);

        List<Image> updatedImages = imageService.updateImages(
                existing.getImages(),
                dto.remainingImages(),
                newFiles,
                "products"
        );

        existing.getImages().clear();


        existing.getImages().addAll(updatedImages);

        var saved = productRepository.save(existing);
        log.info("Updated product: id={}, title='{}'", saved.getId(), saved.getTitle());

        return mapper.toProductCardResponse(saved, existing.getMainImage());
    }

    private void updateProductFields(Product existing, ProductRequest dto) {
        existing.setTitle(dto.title());
        existing.setSlug(SlugUtils.toSlug(dto.title(), existing.getId()));
        existing.setDescription(dto.description());
        existing.setCondition(dto.condition());

        if (existing.getPrice() != null) {
            existing.getPrice().setAmount(dto.amount());
        } else {
            existing.setPrice(Price.builder()
                    .amount(dto.amount())
                    .currencyCode("PLN")
                    .build());
        }

        if (dto.categoryId() != null) {
            existing.setCategory(categoryRepository.getReferenceById(dto.categoryId()));
        }
        if (dto.brandId() != null) {
            existing.setBrand(brandRepository.getReferenceById(dto.brandId()));
        }
        if (dto.categoryId() != null) {
            existing.setColor(colorRepository.getReferenceById(dto.colorId()));
        }
        if (dto.sizeId() != null) {
            existing.setSize(sizeRepository.getReferenceById(dto.sizeId()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(UUID productId) {
        var product = this.productRepository.findByIdWithDetails(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return mapper.toDetailDTO(product);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID productId, Authentication principal) {
        var existing = getProduct(productId);

        if (existing.getStatus() == Status.DELETED) {
            log.info("Product {} is already deleted", productId);
            throw new BusinessException(ErrorCode.PRODUCT_ALREADY_DELETED);
        }

        UUID currentUserId = securityService.getCurrentUserId(principal);

        boolean isOwner = existing.getSeller().getId().equals(currentUserId);

        if (!isOwner) {
            log.warn("SECURITY ALERT: User {} tried to delete product {} owned by seller {}",
                    currentUserId, productId, existing.getSeller().getId());
            throw new BusinessException(ErrorCode.FORBIDDEN_ACTION);
        }

        favoriteRepository.deleteByProductId(productId);
        existing.markAsDeleted();

        log.info("Product successfully deleted: id={}, byUser={}",
                productId, currentUserId);
    }

    @Override
    public Page<ProductCardResponse> getUserProducts(Authentication principal, Pageable pageable) {
        UUID currentUserId = this.securityService.getCurrentUserId(principal);

        Page<Product> productsPage = this.productRepository
                .findAllBySellerIdAndStatusNot(currentUserId, Status.DELETED, pageable);

        return productCardEnricher.enrichPage(productsPage, principal, false);
    }

    @Override
    @Transactional
    public void changeVisibility(UUID productId, Authentication principal) {
        var product = getProduct(productId);

        UUID currentUserId = securityService.getCurrentUserId(principal);

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
        Page<Product> products = productRepository.findFeaturedProducts(seed, pageable);

        return productCardEnricher.enrichPage(products, principal, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCardResponse> search(ProductSearchCriteriaRequest criteria, Pageable pageable, Authentication principal) {
        log.info("Search request received: criteria={}, pageable={}", criteria, pageable);

        if (!StringUtils.hasText(criteria.query()) || criteria.query().trim().length() < 2) {
            throw new BusinessException(ErrorCode.SEARCH_QUERY_TOO_SHORT);
        }

        Pageable unpagedSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Specification<Product> spec = ProductSpecifications.filterBy(criteria);
        Page<Product> products = productRepository.findAll(spec, unpagedSort);

        return productCardEnricher.enrichPage(products, principal, true);
    }

    @Override
    public Page<ProductCardResponse> getSellerProducts(Authentication principal, UUID sellerId, Pageable pageable) {
        Page<Product> products = productRepository.findAllBySellerIdAndStatus(sellerId, Status.ACTIVE, pageable);

        return productCardEnricher.enrichPage(products, principal, true);
    }

    @Async
    public void addViewAsync(UUID productId) {
        productRepository.incrementViews(productId);
    }

    @Override
    public ProductDetailDTO getProductBySlug(String slug) {
        var product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        return mapper.toDetailDTO(product);
    }

    private Product getProduct(UUID productId) {
        return  productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
