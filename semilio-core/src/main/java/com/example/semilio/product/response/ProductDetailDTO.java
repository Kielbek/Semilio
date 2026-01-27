package com.example.semilio.product.response;

import com.example.semilio.product.Condition;
import com.example.semilio.product.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String size;
    private Condition condition;
    private String brand;
    private String color;
    private Integer categoryId;
    private SellerInfoRequest seller;
    private List<String> imageUrls;
    private Integer views;
    private Integer likes;
    private LocalDateTime createdAt;
    private ProductStatus status;
}

