package com.example.semilio.message.response;

import com.example.semilio.image.Image;
import com.example.semilio.image.ImageResponse;
import com.example.semilio.message.MessageState;
import com.example.semilio.message.MessageType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {

    private Long id;
    private String content;
    private ImageResponse mediaFile;
    private MessageType type;
    private MessageState state;
    private String senderId;
    private String receiverId;
    private String chatId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private LocalDateTime createdAt;
    private Map<String, Object> data;
}