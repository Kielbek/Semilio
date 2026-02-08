package com.example.semilio.product.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductStats {
    private Integer views = 0;
    private Integer likes = 0;
    private Integer messages = 0;
}