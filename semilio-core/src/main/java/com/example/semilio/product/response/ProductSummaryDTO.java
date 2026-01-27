package com.example.semilio.product.response;

import com.example.semilio.product.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String size;
    private Condition condition;
    private String categoryName;
    private String mainImageUrl;
}