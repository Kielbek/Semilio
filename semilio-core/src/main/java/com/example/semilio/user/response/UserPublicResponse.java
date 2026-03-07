package com.example.semilio.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserPublicResponse(
        UUID id,
        String nickName,
        String bio,
        String countryName,
        String profilePictureUrl,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        LocalDateTime createdDate
) {
}