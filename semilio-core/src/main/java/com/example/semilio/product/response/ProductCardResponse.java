package com.example.semilio.product.response;

import com.example.semilio.image.response.ImageResponse;
import com.example.semilio.product.enums.Condition;
import com.example.semilio.product.enums.Status;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ProductCardResponse(
        UUID id,
        String title,
        String slug,
        PriceResponse price,
        ImageResponse mainImage,
        String size,
        Condition condition,
        ProductStatsResponse stats,
        Status status,
        boolean isLikedByCurrentUser
) {
}