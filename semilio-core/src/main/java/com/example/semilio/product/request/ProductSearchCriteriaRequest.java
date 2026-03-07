package com.example.semilio.product.request;

import java.math.BigDecimal;

public record ProductSearchCriteriaRequest(
        String query,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Long categoryId,
        Long sizeId,
        Long brandId,
        Long colorId,
        String condition,
        String sort
) {}