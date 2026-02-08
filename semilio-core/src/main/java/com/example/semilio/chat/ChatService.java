package com.example.semilio.chat;

import com.example.semilio.chat.response.ChatDetailResponse;
import com.example.semilio.chat.response.ChatListResponse;
import com.example.semilio.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface ChatService {
    Page<ChatListResponse> getChatsByReceiverId(Authentication principal, Pageable pageable);

    Chat createChat(String senderId, Product product);

    ChatDetailResponse getChatsById(String chatId, Authentication principal);

    void validateChatAccess(Chat chat, String userId);

    Chat loadChatByIdWithDetails(String chatId);

    ChatDetailResponse getChatsByProductId(String productId, Authentication principal);
}
