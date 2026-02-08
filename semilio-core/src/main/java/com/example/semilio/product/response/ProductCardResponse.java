package com.example.semilio.product.response;

import com.example.semilio.image.ImageResponse;
import com.example.semilio.product.Condition;
import com.example.semilio.product.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCardResponse {
    private String id;

    private String title;

    private String slug;

    private PriceResponse price;

    private ImageResponse mainImage;

    private String size;

    private Condition condition;

    private ProductStatsResponse stats;

    private Status status;

    boolean isLikedByCurrentUser;
}
