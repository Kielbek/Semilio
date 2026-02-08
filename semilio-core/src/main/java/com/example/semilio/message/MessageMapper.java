package com.example.semilio.message;

import com.example.semilio.image.ImageMapper;
import com.example.semilio.message.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageMapper {

    private final ImageMapper imageMapper;

    public MessageResponse toMessageResponse(Message message) {
        if (message == null) return null;

        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .mediaFile(imageMapper.imageTOImageResponse(message.getMediaFile()))
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .chatId(message.getChat().getId())
                .type(message.getType())
                .state(message.getState())
                .createdAt(message.getCreatedDate())
                .build();
    }
}