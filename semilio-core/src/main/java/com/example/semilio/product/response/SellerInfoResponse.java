package com.example.semilio.product.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerInfoResponse {
    private String id;
    private String nickName;
    private String profilePictureUrl;
    private String countryName;
}
