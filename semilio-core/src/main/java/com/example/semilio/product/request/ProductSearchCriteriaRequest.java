package com.example.semilio.product.request;

import java.math.BigDecimal;

public record ProductSearchCriteriaRequest(
        String query,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String category,
        String productSize,
        String condition
) {}