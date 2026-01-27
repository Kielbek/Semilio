package com.example.semilio.favorite;

import com.example.semilio.product.response.ProductCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{productId}")
    public ResponseEntity<Void> toggleFavorite(
            @PathVariable Long productId,
            Authentication principal
    ) {
        favoriteService.toggleFavorite(productId, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<ProductCardResponse>> getFavorites(
            Authentication principal,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(favoriteService.getUserFavorites(principal, pageable));
    }
}
