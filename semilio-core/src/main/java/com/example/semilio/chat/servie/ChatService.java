package com.example.semilio.chat.servie;

import com.example.semilio.chat.model.Chat;
import com.example.semilio.chat.response.ChatDetailResponse;
import com.example.semilio.chat.response.ChatListResponse;
import com.example.semilio.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface ChatService {
    Page<ChatListResponse> getChatsByReceiverId(Authentication principal, Pageable pageable);

    Chat createChat(UUID senderId, Product product);

    ChatDetailResponse getChatsById(Long chatId, Authentication principal);

    ChatListResponse getSingleChatListResponse(Long chatId, Authentication principal);

    void markChatAsRead(Long chatId, UUID userId);

    void validateChatAccess(Chat chat, UUID userId);

    Chat loadChatByIdWithDetails(Long chatId);

    ChatDetailResponse getChatsByProductId(UUID productId, Authentication principal);
}
