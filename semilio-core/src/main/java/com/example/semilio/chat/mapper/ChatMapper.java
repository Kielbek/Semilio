package com.example.semilio.chat.mapper;

import com.example.semilio.chat.model.Chat;
import com.example.semilio.chat.response.ChatDetailResponse;
import com.example.semilio.chat.response.ChatListResponse;
import com.example.semilio.image.mapper.ImageMapper;
import com.example.semilio.product.mapper.ProductMapper;
import com.example.semilio.product.model.Product; // Dodany import
import com.example.semilio.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatMapper {

    private final ProductMapper productMapper;
    private final ImageMapper imageMapper;

    public ChatListResponse toListResponse(Chat chat, Product product, User interlocutor, UUID currentUserId) {
        return ChatListResponse.builder()
                .id(chat.getId())
                .productId(product.getId())
                .productTitle(product.getTitle())
                .productImage(imageMapper.imageTOImageResponse(product.getMainImage()))

                .interlocutorName(interlocutor.getNickName())
                .interlocutorImage(interlocutor.getProfilePictureUrl())

                .unreadCount(currentUserId.equals(chat.getBuyerId()) ? chat.getBuyerUnreadCount() : chat.getSellerUnreadCount())
//                .lastMessage(chat.getLastMessagePreview())
                .lastMessageDate(chat.getLastModifiedDate())
                .build();
    }

    public ChatDetailResponse toDetailResponse(Chat chat, Product product, UUID currentUserId) {
        if (chat == null) return null;

        boolean isBuyer = currentUserId.equals(chat.getBuyerId());

        return ChatDetailResponse.builder()
                .id(chat.getId())
                .productId(chat.getProductId())
                .productTitle(product != null ? product.getTitle() : null)
                .productPrice(product != null ? productMapper.mapToPriceResponse(product.getPrice()) : null)
                .productImage((product != null && product.getMainImage() != null)
                        ? imageMapper.imageTOImageResponse(product.getMainImage())
                        : null)
                .recipientId(isBuyer ? chat.getSellerId() : chat.getBuyerId())
                .build();
    }
}