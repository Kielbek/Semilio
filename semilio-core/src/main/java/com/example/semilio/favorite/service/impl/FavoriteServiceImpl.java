package com.example.semilio.favorite.service.impl;

import com.example.semilio.favorite.model.Favorite;
import com.example.semilio.favorite.repository.FavoriteRepository;
import com.example.semilio.favorite.service.FavoriteService;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.mapper.ProductMapper;
import com.example.semilio.product.repository.ProductRepository;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.service.impl.ProductCardEnricher;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.model.User;
import com.example.semilio.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final SecurityService securityService;
     private final ProductCardEnricher productCardEnricher;

    @Override
    @Transactional
    public void toggleFavorite(UUID productId, Authentication principal) {
        UUID userId = securityService.getCurrentUserId(principal);

        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {

            favoriteRepository.deleteByUserIdAndProductId(userId, productId);
            productRepository.decrementLikes(productId);
        } else {
            Favorite favorite = Favorite.builder()
                    .userId(userId)
                    .productId(productId)
                    .createdAt(LocalDateTime.now())
                    .build();

            favoriteRepository.save(favorite);
            productRepository.incrementLikes(productId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCardResponse> getUserFavorites(Authentication principal, Pageable pageable) {
        UUID userId = securityService.getCurrentUserId(principal);

        Page<Favorite> favoritesPage = favoriteRepository.findAllByUserId(userId, pageable);

        List<UUID> productIds = favoritesPage.getContent().stream()
                .map(Favorite::getProductId)
                .toList();

        List<Product> products = productRepository.findAllById(productIds);

        Page<Product> productsPage = new PageImpl<>(products, pageable, favoritesPage.getTotalElements());

        return productCardEnricher.enrichFavoritesPage(productsPage);
    }
}
