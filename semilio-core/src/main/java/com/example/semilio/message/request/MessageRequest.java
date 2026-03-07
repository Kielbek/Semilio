package com.example.semilio.message.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record MessageRequest(
        Long chatId,
        UUID productId,

        @NotBlank(message = "VALIDATION.MESSAGE.CONTENT.BLANK")
        @Size(max = 250, message = "VALIDATION.MESSAGE.CONTENT.SIZE")
        String content
) {}