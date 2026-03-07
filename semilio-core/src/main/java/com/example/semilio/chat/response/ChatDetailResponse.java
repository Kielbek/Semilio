package com.example.semilio.chat.response;

import com.example.semilio.image.response.ImageResponse;
import com.example.semilio.product.response.PriceResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ChatDetailResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,
        String chatName,
        UUID productId,
        String productTitle,
        PriceResponse productPrice,
        ImageResponse productImage,
        UUID recipientId
) {
}