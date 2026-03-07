package com.example.semilio.product.response;

import lombok.Builder;

@Builder
public record ProductStatsResponse(
        Integer views,
        Integer likes
) {
}