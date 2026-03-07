package com.example.semilio.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SmsVerificationRequest(
        @NotBlank(message = "VALIDATION.AUTHENTICATION.SMS.CODE_NOT_BLANK")
        @Size(min = 6, max = 6, message = "VALIDATION.AUTHENTICATION.SMS.CODE_INVALID_LENGTH")
        @Pattern(regexp = "^\\d{6}$", message = "VALIDATION.AUTHENTICATION.SMS.CODE_INVALID_FORMAT")
        String code
) {
}