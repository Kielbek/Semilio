package com.example.semilio.chat;

import com.example.semilio.chat.response.ChatDetailResponse;
import com.example.semilio.chat.response.ChatListResponse;
import com.example.semilio.image.ImageMapper;
import com.example.semilio.product.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ChatMapper {

    private final ProductMapper productMapper;
    private final ImageMapper imageMapper;

    public ChatListResponse toListResponse(Chat chat, String currentUserId) {
        if (chat == null) return null;

        return ChatListResponse.builder()
                .id(chat.getId())
                .lastMessage(chat.getLastMessageContent())
                .lastMessageDate(chat.getLastMessageDate())
                .unreadCount(chat.getUnreadMessages(currentUserId))
                .productTitle(chat.getProduct() != null ? chat.getProduct().getTitle() : null)
                .productImage(chat.getProduct() != null ? imageMapper.imageTOImageResponse(
                        chat
                                .getProduct()
                                .getMainImage()) : null)
                .productId(chat.getProduct() != null ? chat.getProduct().getId() : null)
                .build();
    }

    public ChatDetailResponse toDetailResponse(Chat chat, String currentUserId) {
        if (chat == null) return null;

        return ChatDetailResponse.builder()
                .id(chat.getId())
                .productId(chat.getProduct() != null ? chat.getProduct().getId() : null)
                .productTitle(chat.getProduct() != null ? chat.getProduct().getTitle() : null)
                .productPrice(chat.getProduct() != null ? productMapper.mapToPriceResponse(chat.getProduct().getPrice()) : null)
                .productImage(chat.getProduct() != null ? chat.getProduct().getMainImageUrl() : null)
                .recipientId(chat.getSender().getId().equals(currentUserId)
                        ? chat.getRecipient().getId()
                        : chat.getSender().getId())
                .build();
    }
}