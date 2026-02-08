package com.example.semilio.favorite.impl;

import com.example.semilio.favorite.Favorite;
import com.example.semilio.favorite.FavoriteRepository;
import com.example.semilio.favorite.FavoriteService;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.ProductMapper;
import com.example.semilio.product.ProductRepository;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.User;
import com.example.semilio.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public void toggleFavorite(String productId, Authentication principal) {
        String userId = securityService.getCurrentUserId(principal);

        if (favoriteRepository.existsByUserIdAndProductId(userId, productId)) {

            favoriteRepository.deleteByUserIdAndProductId(userId, productId);
            productRepository.decrementLikes(productId);
        } else {
            User userRef = userRepository.getReferenceById(userId);
            Product productRef = productRepository.getReferenceById(productId);

            Favorite favorite = Favorite.builder()
                    .user(userRef)
                    .product(productRef)
                    .createdAt(LocalDateTime.now())
                    .build();

            favoriteRepository.save(favorite);
            productRepository.incrementLikes(productId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCardResponse> getUserFavorites(Authentication principal, Pageable pageable) {
        String userId = securityService.getCurrentUserId(principal);

        Page<Favorite> favoritesPage = this.favoriteRepository.findAllByUserId(userId, pageable);

        return favoritesPage.map(favorite ->
                    this.mapper.toProductCardResponse(favorite.getProduct(), true)
        );
    }
}
