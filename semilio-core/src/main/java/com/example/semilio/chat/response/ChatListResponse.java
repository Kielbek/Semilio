package com.example.semilio.chat.response;

import com.example.semilio.image.Image;
import com.example.semilio.image.ImageResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatListResponse {
    private String id;
    private String productTitle;
    private String lastMessage;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime lastMessageDate;
    private long unreadCount;
    private ImageResponse productImage;
    private String productId;
}