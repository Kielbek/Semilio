package com.example.semilio.auth.request;

import lombok.Builder;

@Builder
public record RefreshRequest(
        String refreshToken
) {
}