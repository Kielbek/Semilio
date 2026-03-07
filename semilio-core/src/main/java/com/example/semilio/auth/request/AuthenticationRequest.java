package com.example.semilio.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record AuthenticationRequest(
        @NotBlank(message = "VALIDATION.AUTHENTICATION.EMAIL.NOT_BLANK")
        @Email(message = "VALIDATION.AUTHENTICATION.EMAIL.FORMAT")
        String email,

        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
                message = "VALIDATION.REGISTRATION.PASSWORD.WEAK")
        @NotBlank(message = "VALIDATION.AUTHENTICATION.PASSWORD.NOT_BLANK")
        String password
) {
}