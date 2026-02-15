package com.example.semilio.chat.response;

import com.example.semilio.image.ImageResponse;
import com.example.semilio.product.response.PriceResponse;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDetailResponse {
    private String id;
    private String chatName;
    private String productId;
    private String productTitle;
    private PriceResponse productPrice;
    private ImageResponse productImage;
    private String recipientId;
}