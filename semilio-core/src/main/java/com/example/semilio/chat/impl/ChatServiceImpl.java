package com.example.semilio.chat.impl;

import com.example.semilio.chat.Chat;
import com.example.semilio.chat.ChatMapper;
import com.example.semilio.chat.ChatRepository;
import com.example.semilio.chat.ChatService;
import com.example.semilio.chat.response.ChatResponse;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.User;
import com.example.semilio.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public Page<ChatResponse> getChatsByReceiverId(Authentication principal, Pageable pageable) {
        final String userId = securityService.getCurrentUserId(principal);
        log.info("Fetching all chats for user: {}", userId);

        Page<Chat> chats = chatRepository.findChatsBySenderId(userId, pageable);

        return chats.map(chat -> mapper.toChatResponse(chat, userId));
    }

    @Transactional
    public String createChat(Authentication principal, String receiverId) {
        final String senderId = securityService.getCurrentUserId(principal);
        log.info("Initiating chat creation: sender={}, receiver={}", senderId, receiverId);

        Optional<Chat> existingChat = chatRepository.findChatByReceiverAndSender(senderId, receiverId);
        if (existingChat.isPresent()) {
            log.info("Existing chat found with ID: {}", existingChat.get().getId());
            return existingChat.get().getId();
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found with ID: " + senderId));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found with ID: " + receiverId));

        Chat chat = new Chat();
        chat.setSender(sender);
        chat.setRecipient(receiver);

        Chat savedChat = chatRepository.save(chat);
        log.info("Successfully created new chat with ID: {}", savedChat.getId());

        return savedChat.getId();
    }
}