package com.example.semilio.chat.servie.impl;

import com.example.semilio.chat.mapper.ChatMapper;
import com.example.semilio.chat.model.Chat;
import com.example.semilio.chat.repository.ChatRepository;
import com.example.semilio.chat.response.ChatDetailResponse;
import com.example.semilio.chat.response.ChatListResponse;
import com.example.semilio.chat.servie.ChatService;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.repository.ProductRepository;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.model.User;
import com.example.semilio.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final ChatMapper mapper;

    public Page<ChatListResponse> getChatsByReceiverId(Authentication principal, Pageable pageable) {
        final UUID currentUserId = securityService.getCurrentUserId(principal);

        Pageable sortedByDate = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("lastModifiedDate").descending().and(pageable.getSort())
        );

        Page<Chat> chats = chatRepository.findAllByBuyerIdOrSellerId(currentUserId, currentUserId, sortedByDate);

        if (chats.isEmpty()) {
            return Page.empty(pageable);
        }

        List<UUID> productIds = chats.stream().map(Chat::getProductId).distinct().toList();
        Map<UUID, Product> productsMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        List<UUID> interlocutorIds = chats.stream()
                .map(chat -> chat.getBuyerId().equals(currentUserId) ? chat.getSellerId() : chat.getBuyerId())
                .distinct()
                .toList();

        Map<UUID, User> usersMap = userRepository.findAllById(interlocutorIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u));

        return chats.map(chat -> {
            Product product = productsMap.get(chat.getProductId());

            UUID otherUserId = chat.getBuyerId().equals(currentUserId) ? chat.getSellerId() : chat.getBuyerId();
            User interlocutor = usersMap.get(otherUserId);

            return mapper.toListResponse(chat, product, interlocutor, currentUserId);
        });
    }

    @Override
    @Transactional
    public Chat createChat(UUID senderId, Product product) {
        final UUID buyerId = senderId;
        final UUID sellerId = product.getSeller().getId();

        log.info("Resolving chat: buyer={}, seller={}, productId={}", buyerId, sellerId, product.getId());

        if (buyerId.equals(sellerId)) {
            throw new BusinessException(ErrorCode.CHAT_ACCESS_DENIED);
        }

        return chatRepository.findByProductIdAndBuyerId(product.getId(), buyerId)
                .orElseGet(() -> {
                    log.info("Creating new marketplace chat for product ID: {}", product.getId());

                    Chat newChat = new Chat();
                    newChat.setProductId(product.getId());
                    newChat.setBuyerId(buyerId);
                    newChat.setSellerId(sellerId);
                    newChat.setProductId(product.getId());

                    return chatRepository.save(newChat);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ChatDetailResponse getChatsById(Long chatId, Authentication principal) {
        final UUID currentUserId = securityService.getCurrentUserId(principal);

        Chat chat = loadChatByIdWithDetails(chatId);
        validateChatAccess(chat, currentUserId);

        Product product = productRepository.findById(chat.getProductId()).orElse(null);

        return mapper.toDetailResponse(chat, product, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatDetailResponse getChatsByProductId(UUID productId, Authentication principal) {
        final UUID currentUserId = securityService.getCurrentUserId(principal);

        return chatRepository.findByProductIdAndBuyerId(productId, currentUserId)
                .map(chat -> {
                    validateChatAccess(chat, currentUserId);

                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

                    return mapper.toDetailResponse(chat, product, currentUserId);
                })
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatListResponse getSingleChatListResponse(Long chatId, Authentication principal) {
        final UUID currentUserId = securityService.getCurrentUserId(principal);

        Chat chat = loadChatByIdWithDetails(chatId);
        validateChatAccess(chat, currentUserId);

        Product product = productRepository.findById(chat.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        UUID otherUserId = chat.getBuyerId().equals(currentUserId) ? chat.getSellerId() : chat.getBuyerId();
        User interlocutor = userRepository.findById(otherUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return mapper.toListResponse(chat, product, interlocutor, currentUserId);
    }

    @Override
    @Transactional
    public void markChatAsRead(Long chatId, UUID userId) {
        chatRepository.resetUnreadCount(chatId, userId);
    }

    @Override
    public void validateChatAccess(Chat chat, UUID userId) {
        if (!chat.getBuyerId().equals(userId) && !chat.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.CHAT_ACCESS_DENIED);
        }
    }

    @Override
    public Chat loadChatByIdWithDetails(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_FOUND));
    }
}