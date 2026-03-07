package com.example.semilio.product.response;

import lombok.Builder;
import java.util.UUID;

@Builder
public record SellerInfoResponse(
        UUID id,
        String nickName,
        String profilePictureUrl,
        String countryName
) {
}