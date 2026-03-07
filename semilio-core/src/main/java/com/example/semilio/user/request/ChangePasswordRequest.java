package com.example.semilio.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChangePasswordRequest(
        @NotBlank(message = "VALIDATION.USER.PASSWORD.CURRENT_BLANK")
        String currentPassword,

        @NotBlank(message = "VALIDATION.USER.PASSWORD.NEW_BLANK")
        @Size(min = 8, max = 72, message = "VALIDATION.USER.PASSWORD.SIZE")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
                message = "VALIDATION.USER.PASSWORD.WEAK")
        String newPassword,

        @NotBlank(message = "VALIDATION.USER.PASSWORD.CONFIRM_BLANK")
        @Size(min = 8, max = 72, message = "VALIDATION.USER.PASSWORD.SIZE")
        String confirmNewPassword
) {

    public ChangePasswordRequest {
        if (newPassword != null && !newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }
}