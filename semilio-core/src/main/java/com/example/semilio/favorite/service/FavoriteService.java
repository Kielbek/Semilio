package com.example.semilio.favorite.service;

import com.example.semilio.product.response.ProductCardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface FavoriteService {

    void toggleFavorite(UUID productId, Authentication principal);

    Page<ProductCardResponse> getUserFavorites(Authentication principal, Pageable pageable);
}

