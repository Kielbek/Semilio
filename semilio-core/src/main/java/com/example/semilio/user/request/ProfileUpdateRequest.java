package com.example.semilio.user.request;

import lombok.Builder;

@Builder
public record ProfileUpdateRequest(
        String nickName,
        String bio,
        String countryCode
) {
}