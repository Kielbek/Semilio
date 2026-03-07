package com.example.semilio.message.event;

import com.example.semilio.message.model.payload.MessagePayload;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record MessageSentEvent(
        String id,
        String chatId,
        UUID senderId,
        UUID recipientId,
        LocalDateTime createdAt,
        MessagePayload payload
) {}