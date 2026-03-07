package com.example.semilio.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ResetPasswordRequest(
        @NotBlank(message = "VALIDATION.AUTHENTICATION.RESET_PASSWORD.TOKEN.NOT_BLANK")
        String token,

        @NotBlank(message = "VALIDATION.REGISTRATION.PASSWORD.BLANK")
        @Size(min = 8, max = 72, message = "VALIDATION.REGISTRATION.PASSWORD.SIZE")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
                message = "VALIDATION.REGISTRATION.PASSWORD.WEAK")
        String password,

        @NotBlank(message = "VALIDATION.REGISTRATION.CONFIRM_PASSWORD.BLANK")
        @Size(min = 8, max = 72, message = "VALIDATION.REGISTRATION.CONFIRM_PASSWORD.SIZE")
        String confirmPassword
) {
}