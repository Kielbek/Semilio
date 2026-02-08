package com.example.semilio.notification.response;

import com.example.semilio.notification.NotificationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private NotificationType type;
    private String title;
    private String content;
    private String targetUrl;
    private String imageUrl;
    private boolean read;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private Map<String, Object> data;
}
