package com.example.semilio.product.request;

import com.example.semilio.product.enums.Condition;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProductRequest(
        @NotBlank(message = "VALIDATION.PRODUCT.TITLE.BLANK")
        @Size(min = 5, max = 255, message = "VALIDATION.PRODUCT.TITLE.SIZE")
        String title,

        @NotBlank(message = "VALIDATION.PRODUCT.DESCRIPTION.BLANK")
        @Size(min = 20, max = 2000, message = "VALIDATION.PRODUCT.DESCRIPTION.SIZE")
        String description,

        @NotNull(message = "VALIDATION.PRODUCT.PRICE.NOT_NULL")
        @DecimalMin(value = "0.01", message = "VALIDATION.PRODUCT.PRICE.MIN")
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount,

        @NotNull(message = "VALIDATION.PRODUCT.SIZE.NOT_NULL")
        Long sizeId,

        @NotNull(message = "VALIDATION.PRODUCT.BRAND.NOT_NULL")
        Long brandId,

        @NotNull(message = "VALIDATION.PRODUCT.COLOR.NOT_NULL")
        Long colorId,

        @NotNull(message = "VALIDATION.PRODUCT.CATEGORY.NOT_NULL")
        Long categoryId,

        @NotNull(message = "VALIDATION.PRODUCT.CONDITION.NOT_NULL")
        Condition condition,

        List<String> remainingImages
) {}