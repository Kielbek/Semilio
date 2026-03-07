package com.example.semilio.product.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
public record PriceResponse(
    BigDecimal amount,
    String currencyCode
) {
}
