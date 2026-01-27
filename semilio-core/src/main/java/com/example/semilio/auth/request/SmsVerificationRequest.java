package com.example.semilio.auth.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsVerificationRequest {

    @NotNull(message = "Verification code must not be null")
    Integer code;
}
