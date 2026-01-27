package com.example.semilio.chat;

import com.example.semilio.chat.response.ChatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface ChatService {
    Page<ChatResponse> getChatsByReceiverId(Authentication principal, Pageable pageable);
    String createChat(Authentication principal, String receiverId);
}
