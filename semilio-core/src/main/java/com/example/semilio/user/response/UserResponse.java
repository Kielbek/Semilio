package com.example.semilio.user.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String firstName,
        String lastName,
        String nickName,
        String email,
        String phoneNumber,
        String bio,
        LocalDate dateOfBirth,
        String countryName,
        boolean enabled,
        boolean locked,
        boolean credentialsExpired,
        boolean emailVerified,
        boolean phoneVerified,
        String profilePictureUrl,
        List<String> roles,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        LocalDateTime createdDate
) {
}