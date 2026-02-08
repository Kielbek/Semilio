package com.example.semilio.product.response;

import com.example.semilio.image.ImageResponse;
import com.example.semilio.product.Color;
import com.example.semilio.product.Condition;
import com.example.semilio.product.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {
    private String id;
    private String title;
    private String slug;
    private String description;
    private PriceResponse price;
    private String size;
    private Condition condition;
    private String brand;
    private Color color;
    private Integer categoryId;
    private SellerInfoResponse seller;
    private List<ImageResponse> images;
    private ProductStatsResponse stats;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private Status status;
}

