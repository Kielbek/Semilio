package com.example.semilio.favorite;

import com.example.semilio.product.response.ProductCardResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface FavoriteService {

    void toggleFavorite(String productId, Authentication principal);

    Page<ProductCardResponse> getUserFavorites(Authentication principal, Pageable pageable);
}

