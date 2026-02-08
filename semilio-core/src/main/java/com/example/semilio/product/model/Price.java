package com.example.semilio.product.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price {

    @NotNull
    @Column(nullable = false)
    private BigDecimal amount;

    @NotBlank
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

}