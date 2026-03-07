package com.example.semilio.message.mapper;

import com.example.semilio.message.model.Message;
import com.example.semilio.message.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageMapper {

    public MessageResponse toMessageResponse(Message message) {
        if (message == null) return null;

        return new MessageResponse(
                message.getId(),
                message.getChatId(),
                message.getSenderId(),
                message.getCreatedDate(),
                message.getPayload()
        );
    }

}