package com.example.semilio.product.response;

import com.example.semilio.product.Condition;
import com.example.semilio.product.ProductStats;
import com.example.semilio.product.ProductStatus;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private BigDecimal price;

    private String mainImageUrl;

    private String size;

    private Condition condition;

    private ProductStats stats;

    private ProductStatus status;

    boolean isLikedByCurrentUser;
}
