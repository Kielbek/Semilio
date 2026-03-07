package com.example.semilio.favorite.service.impl;

import com.example.semilio.favorite.model.Favorite;
import com.example.semilio.favorite.repository.FavoriteRepository;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.repository.ProductRepository;
import com.example.semilio.product.response.ProductCardResponse;
import com.example.semilio.product.service.impl.ProductCardEnricher;
import com.example.semilio.service.SecurityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private ProductCardEnricher productCardEnricher;

    @InjectMocks
    private FavoriteServiceImpl favoriteService;

    @Nested
    @DisplayName("Toggle Favorite Tests")
    class ToggleFavoriteTests {

        @Test
        @DisplayName("Should delete favorite and decrement likes when favorite already exists")
        void shouldDeleteFavoriteAndDecrementLikesWhenFavoriteExists() {
            UUID productId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            given(securityService.getCurrentUserId(principal)).willReturn(userId);
            given(favoriteRepository.existsByUserIdAndProductId(userId, productId)).willReturn(true);

            favoriteService.toggleFavorite(productId, principal);

            then(favoriteRepository).should(times(1)).deleteByUserIdAndProductId(userId, productId);
            then(productRepository).should(times(1)).decrementLikes(productId);
            then(favoriteRepository).should(never()).save(any());
            then(productRepository).should(never()).incrementLikes(any());
        }

        @Test
        @DisplayName("Should save favorite and increment likes when favorite does not exist")
        void shouldSaveFavoriteAndIncrementLikesWhenFavoriteDoesNotExist() {
            UUID productId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            given(securityService.getCurrentUserId(principal)).willReturn(userId);
            given(favoriteRepository.existsByUserIdAndProductId(userId, productId)).willReturn(false);

            favoriteService.toggleFavorite(productId, principal);

            then(favoriteRepository).should(times(1)).save(any(Favorite.class));
            then(productRepository).should(times(1)).incrementLikes(productId);
            then(favoriteRepository).should(never()).deleteByUserIdAndProductId(any(), any());
            then(productRepository).should(never()).decrementLikes(any());
        }
    }

    @Nested
    @DisplayName("Get User Favorites Tests")
    class GetUserFavoritesTests {

        @Test
        @DisplayName("Should return enriched page of favorites for current user")
        @SuppressWarnings("unchecked")
        void shouldReturnEnrichedPageOfFavoritesForCurrentUser() {
            UUID userId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            Pageable pageable = PageRequest.of(0, 10);

            Favorite favorite = Favorite.builder().productId(productId).userId(userId).build();
            Page<Favorite> favoritesPage = new PageImpl<>(List.of(favorite));

            Product product = new Product();
            product.setId(productId);
            List<Product> productsList = List.of(product);

            ProductCardResponse cardResponse = ProductCardResponse.builder().id(productId).build();
            Page<ProductCardResponse> expectedPage = new PageImpl<>(List.of(cardResponse));

            given(securityService.getCurrentUserId(principal)).willReturn(userId);
            given(favoriteRepository.findAllByUserId(userId, pageable)).willReturn(favoritesPage);
            given(productRepository.findAllById(List.of(productId))).willReturn(productsList);
            given(productCardEnricher.enrichFavoritesPage(any(Page.class))).willReturn(expectedPage);

            Page<ProductCardResponse> result = favoriteService.getUserFavorites(principal, pageable);

            assertThat(result).isNotNull().isEqualTo(expectedPage);
            then(favoriteRepository).should(times(1)).findAllByUserId(userId, pageable);
            then(productRepository).should(times(1)).findAllById(List.of(productId));
            then(productCardEnricher).should(times(1)).enrichFavoritesPage(any(Page.class));
        }

        @Test
        @DisplayName("Should return empty page when user has no favorites")
        @SuppressWarnings("unchecked")
        void shouldReturnEmptyPageWhenUserHasNoFavorites() {
            UUID userId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            Pageable pageable = PageRequest.of(0, 10);

            Page<Favorite> emptyFavoritesPage = new PageImpl<>(List.of(), pageable, 0);
            Page<ProductCardResponse> expectedEmptyPage = new PageImpl<>(List.of(), pageable, 0);

            given(securityService.getCurrentUserId(principal)).willReturn(userId);
            given(favoriteRepository.findAllByUserId(userId, pageable)).willReturn(emptyFavoritesPage);
            given(productRepository.findAllById(List.of())).willReturn(List.of());
            given(productCardEnricher.enrichFavoritesPage(any(Page.class))).willReturn(expectedEmptyPage);

            Page<ProductCardResponse> result = favoriteService.getUserFavorites(principal, pageable);

            assertThat(result).isNotNull().isEqualTo(expectedEmptyPage);
            then(favoriteRepository).should(times(1)).findAllByUserId(userId, pageable);
            then(productRepository).should(times(1)).findAllById(List.of());
            then(productCardEnricher).should(times(1)).enrichFavoritesPage(any(Page.class));
        }

        @Test
        @DisplayName("Should handle missing products gracefully when products were deleted but remain in favorites")
        @SuppressWarnings("unchecked")
        void shouldHandleMissingProductsGracefully() {
            UUID userId = UUID.randomUUID();
            UUID existingProductId = UUID.randomUUID();
            UUID deletedProductId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            Pageable pageable = PageRequest.of(0, 10);

            Favorite fav1 = Favorite.builder().productId(existingProductId).userId(userId).build();
            Favorite fav2 = Favorite.builder().productId(deletedProductId).userId(userId).build();
            Page<Favorite> favoritesPage = new PageImpl<>(List.of(fav1, fav2), pageable, 2);

            Product existingProduct = new Product();
            existingProduct.setId(existingProductId);
            List<Product> foundProducts = List.of(existingProduct);

            ProductCardResponse cardResponse = ProductCardResponse.builder().id(existingProductId).build();
            Page<ProductCardResponse> expectedPage = new PageImpl<>(List.of(cardResponse), pageable, 2);

            given(securityService.getCurrentUserId(principal)).willReturn(userId);
            given(favoriteRepository.findAllByUserId(userId, pageable)).willReturn(favoritesPage);

            given(productRepository.findAllById(List.of(existingProductId, deletedProductId))).willReturn(foundProducts);
            given(productCardEnricher.enrichFavoritesPage(any(Page.class))).willReturn(expectedPage);

            Page<ProductCardResponse> result = favoriteService.getUserFavorites(principal, pageable);

            assertThat(result).isNotNull().isEqualTo(expectedPage);
            then(favoriteRepository).should(times(1)).findAllByUserId(userId, pageable);
            then(productRepository).should(times(1)).findAllById(List.of(existingProductId, deletedProductId));
        }
    }
}