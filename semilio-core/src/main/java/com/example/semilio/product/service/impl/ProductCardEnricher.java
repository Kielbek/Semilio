package com.example.semilio.product.service.impl;

import com.example.semilio.favorite.repository.FavoriteRepository;
import com.example.semilio.image.model.Image;
import com.example.semilio.message.projection.ProductMainImageProjection;
import com.example.semilio.product.mapper.ProductMapper;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.repository.ProductRepository;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductCardEnricher {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final SecurityService securityService;
    private final ProductMapper mapper;

    public Page<ProductCardResponse> enrichPage(Page<Product> productsPage, Authentication principal, boolean checkLikes) {
        if (productsPage.isEmpty()) {
            return productsPage.map(product -> mapper.toProductCardResponse(product, null, false));
        }
        List<UUID> visibleIds = productsPage.getContent().stream()
                .map(Product::getId)
                .toList();

        Set<UUID> likedIds = getLikedIds(visibleIds, principal, checkLikes);
        Map<UUID, Image> imageMap = getMainImages(visibleIds);

        return productsPage.map(product -> mapper.toProductCardResponse(
                product,
                imageMap.get(product.getId()),
                likedIds.contains(product.getId())
        ));
    }

    public Page<ProductCardResponse> enrichFavoritesPage(Page<Product> productsPage) {
        if (productsPage.isEmpty()) {
            return productsPage.map(product -> mapper.toProductCardResponse(product, null, true));
        }

        List<UUID> visibleIds = productsPage.getContent().stream()
                .map(Product::getId)
                .toList();

        Map<UUID, Image> imageMap = getMainImages(visibleIds);

        return productsPage.map(product -> mapper.toProductCardResponse(
                product,
                imageMap.get(product.getId()),
                true
        ));
    }

    private Set<UUID> getLikedIds(List<UUID> productIds, Authentication principal, boolean checkLikes) {
        if (!checkLikes || principal == null) return new HashSet<>();

        UUID userId = securityService.getCurrentUserId(principal);
        return favoriteRepository.findLikedProductIds(userId, productIds);
    }

    private Map<UUID, Image> getMainImages(List<UUID> productIds) {
        return productRepository.findMainImagesForProducts(productIds).stream()
                .collect(Collectors.toMap(
                        ProductMainImageProjection::getProductId,
                        ProductMainImageProjection::getImage,
                        (existing, replacement) -> existing
                ));
    }
}