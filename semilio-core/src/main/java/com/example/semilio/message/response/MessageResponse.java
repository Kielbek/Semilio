package com.example.semilio.message.response;

import com.example.semilio.message.model.payload.MessagePayload;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MessageResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long chatId,
        UUID senderId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        LocalDateTime createdAt,
        MessagePayload payload
) {}
