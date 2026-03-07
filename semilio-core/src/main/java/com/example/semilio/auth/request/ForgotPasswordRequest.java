package com.example.semilio.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ForgotPasswordRequest(
        @NotBlank(message = "VALIDATION.AUTHENTICATION.EMAIL.NOT_BLANK")
        @Email(message = "VALIDATION.AUTHENTICATION.EMAIL.FORMAT")
        String email
) {
}