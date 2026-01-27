package com.example.semilio.chat;

import com.example.semilio.chat.response.ChatResponse;
import com.example.semilio.product.Product;
import org.springframework.stereotype.Service;

@Service
public class ChatMapper {
    public ChatResponse toChatResponse(Chat chat, String senderId) {
        return ChatResponse.builder()
                .id(chat.getId())
                .name(chat.getChatName(senderId))
                .unreadCount(chat.getUnreadMessages(senderId))
                .lastMessage(chat.getLastMessage())
                .lastMessageTime(chat.getLastMessageTime())
//                .isRecipientOnline(chat.getRecipient().isUserOnline())
                .senderId(chat.getSender().getId())
                .receiverId(chat.getRecipient().getId())
                .productTitle(chat.getProduct().getTitle())
                .productMainImageUrl(chat.getProduct().getMainImageUrl())
                .productTitle(chat.getProduct().getTitle())
                .build();
    }
}