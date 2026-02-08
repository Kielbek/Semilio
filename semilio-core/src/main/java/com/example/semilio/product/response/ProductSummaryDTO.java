package com.example.semilio.product.response;

import com.example.semilio.image.ImageResponse;
import com.example.semilio.product.Condition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDTO {
    private String id;
    private String title;
    private PriceResponse price;
    private String size;
    private Condition condition;
    private String categoryName;
    private ImageResponse mainImage;
}