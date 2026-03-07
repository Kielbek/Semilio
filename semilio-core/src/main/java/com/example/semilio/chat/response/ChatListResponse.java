package com.example.semilio.chat.response;

import com.example.semilio.image.response.ImageResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChatListResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,
        String interlocutorName,
        String interlocutorImage,
        String productTitle,
        String lastMessage,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        LocalDateTime lastMessageDate,
        long unreadCount,
        ImageResponse productImage,
        UUID productId
) {
}