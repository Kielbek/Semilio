package com.example.semilio.product.response;

import com.example.semilio.dictionary.model.Brand;
import com.example.semilio.dictionary.model.Color;
import com.example.semilio.dictionary.model.Size;
import com.example.semilio.image.response.ImageResponse;
import com.example.semilio.product.enums.Condition;
import com.example.semilio.product.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ProductDetailDTO(
        UUID id,
        String title,
        String slug,
        String description,
        PriceResponse price,
        Condition condition,
        Size size,
        Brand brand,
        Color color,
        Long categoryId,
        SellerInfoResponse seller,
        List<ImageResponse> images,
        ProductStatsResponse stats,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        LocalDateTime createdAt,
        Status status
) {
}