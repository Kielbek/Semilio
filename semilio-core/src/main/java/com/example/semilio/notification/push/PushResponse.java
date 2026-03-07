package com.example.semilio.notification.push;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PushResponse(
        String id,
        PushType type,
        String title,
        String content,
        String targetUrl,
        String imageUrl,
        Object data,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        LocalDateTime createdAt
) {
}