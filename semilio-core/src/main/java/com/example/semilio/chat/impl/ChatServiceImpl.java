package com.example.semilio.chat.impl;

import com.example.semilio.chat.Chat;
import com.example.semilio.chat.ChatMapper;
import com.example.semilio.chat.ChatRepository;
import com.example.semilio.chat.ChatService;
import com.example.semilio.chat.response.ChatDetailResponse;
import com.example.semilio.chat.response.ChatListResponse;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.product.model.Product;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.User;
import com.example.semilio.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final ChatMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ChatListResponse> getChatsByReceiverId(Authentication principal, Pageable pageable) {
        final String userId = securityService.getCurrentUserId(principal);
        log.info("Fetching all chats for user: {}", userId);

        Pageable sortedByDate = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("lastMessageDate").descending()
        );

        Page<Chat> chats = chatRepository.findAllChatsOrderByLastMessage(userId, sortedByDate);

        return chats.map(chat -> mapper.toListResponse(chat, userId));
    }

    @Override
    @Transactional
    public Chat createChat(String senderId, Product product) {
        final String receiverId = product.getSeller().getId();

        log.info("Resolving chat: sender={}, receiver={}, productId={}", senderId, receiverId, product.getId());

        Optional<Chat> existingChat = chatRepository.findBySenderIdAndProductId(senderId, product.getId());

        if (existingChat.isPresent()) {
            log.info("Existing chat found with ID: {}", existingChat.get().getId());
            return existingChat.get();
        }

        if (senderId.equals(product.getSeller().getId())) {
            throw new BusinessException(ErrorCode.CHAT_ACCESS_DENIED);
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User receiver = product.getSeller();

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setRecipient(receiver);
        chat.setProduct(product);

        Chat savedChat = chatRepository.save(chat);
        log.info("Created new chat with ID: {}", savedChat.getId());

        return savedChat;
    }

    @Override
    public ChatDetailResponse getChatsById(String chatId, Authentication principal) {
        final String currentUserId = securityService.getCurrentUserId(principal);

        Chat chat = loadChatByIdWithDetails(chatId);

        validateChatAccess(chat, currentUserId);

        return mapper.toDetailResponse(chat, currentUserId);
    }

    @Override
    public void validateChatAccess(Chat chat, String userId) {
        if (!chat.getSender().getId().equals(userId) && !chat.getRecipient().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CHAT_ACCESS_DENIED);
        }
    }

    @Override
    public Chat loadChatByIdWithDetails(String chatId) {
        return chatRepository.findByIdWithDetails(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_FOUND));

    }

    @Override
    public ChatDetailResponse getChatsByProductId(String productId, Authentication principal) {
        final String currentUserId = securityService.getCurrentUserId(principal);

        Optional<Chat> chat = chatRepository.findBySenderIdAndProductId(currentUserId, productId);

        if (chat.isEmpty()) {
            return null;
        }

        validateChatAccess(chat.get(), currentUserId);

        return mapper.toDetailResponse(chat.get(), currentUserId);
    }

}