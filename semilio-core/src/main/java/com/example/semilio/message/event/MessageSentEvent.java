package com.example.semilio.message.event;

import com.example.semilio.image.Image;
import com.example.semilio.message.MessageType;
import lombok.Builder;

@Builder
public record MessageSentEvent(
        Long messageId,
        String chatId,
        String senderId,
        String recipientId,
        String content,
        Image mediaFile,
        MessageType type
) {}