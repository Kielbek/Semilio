package com.example.semilio.message.impl;

import com.example.semilio.chat.Chat;
import com.example.semilio.chat.ChatRepository;
import com.example.semilio.chat.ChatService;
import com.example.semilio.comon.validation.FileValidator;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.image.Image;
import com.example.semilio.image.ImageService;
import com.example.semilio.message.*;
import com.example.semilio.message.event.MessageSentEvent;
import com.example.semilio.message.request.MessageRequest;
import com.example.semilio.message.response.MessageResponse;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.ProductRepository;
import com.example.semilio.service.S3Service;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ProductRepository productRepository;
    private final ChatService chatService;
    private final MessageMapper mapper;
    private final SecurityService securityService;
    private final ApplicationEventPublisher eventPublisher;
    private final ImageService imageService;
    private final FileValidator fileValidator;

    @Override
    @Transactional
    public MessageResponse sendMessage(MessageRequest request, Authentication authentication) {
        User sender = securityService.getCurrentUser(authentication);
        Chat chat = resolveChat(sender, request);

        chatService.validateChatAccess(chat, sender.getId());

        Message message = createAndPublishMessage(
                chat,
                sender.getId(),
                request.getContent(),
                MessageType.TEXT,
                null
        );

        return mapper.toMessageResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(String chatId, Pageable pageable, Authentication principal) {
        String currentUserId = securityService.getCurrentUserId(principal);

        Chat chat = chatService.loadChatByIdWithDetails(chatId);

        chatService.validateChatAccess(chat, currentUserId);

        Page<Message> messagesPage = messageRepository.findByChatId(chatId, pageable);

        return messagesPage.map(mapper::toMessageResponse);
    }

    private Chat resolveChat(User sender, MessageRequest request) {
        if (request.getChatId() != null) {
            return chatService.loadChatByIdWithDetails(request.getChatId());
        }

        if (request.getProductId() != null) {
            Product product = productRepository.findByIdWithUser(request.getProductId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            return chatService.createChat(sender.getId(), product);
        }

        throw new BusinessException(ErrorCode.INVALID_CHAT_REQUEST);
    }

    private User determineReceiver(Chat chat, String currentUserId) {
        return chat.getSender().getId().equals(currentUserId) ? chat.getRecipient() : chat.getSender();
    }

    @Override
    @Transactional
    public void setMessagesToSeen(String chatId, Authentication authentication) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_FOUND));
        final String recipientId = getRecipientId(chat, authentication);

//        messageRepository.setMessagesToSeenByChatId(chatId, MessageState.SEEN);
    }

    @Override
    @Transactional
    public MessageResponse uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication) {

        fileValidator.validateImage(file);

        User sender = securityService.getCurrentUser(authentication);
        Chat chat = chatService.loadChatByIdWithDetails(chatId);

        chatService.validateChatAccess(chat, sender.getId());

        Image image = imageService.createImage(file, "message");

        Message message = createAndPublishMessage(
                chat,
                sender.getId(),
                null,
                MessageType.IMAGE,
                image
        );

        return mapper.toMessageResponse(message);
    }

    private Message createAndPublishMessage(Chat chat, String senderId, String content, MessageType type, Image mediaPath) {
        User receiver = determineReceiver(chat, senderId);

        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(senderId);
        message.setReceiverId(receiver.getId());
        message.setContent(content);
        message.setType(type);
        message.setMediaFile(mediaPath);
        message.setState(MessageState.SENT);

        Message savedMessage = messageRepository.save(message);

        chat.setLastMessageContent(type == MessageType.IMAGE ? "Przesłano obraz" : content);
        chat.setLastMessageDate(savedMessage.getCreatedDate());
        chatRepository.save(chat);

        eventPublisher.publishEvent(MessageSentEvent.builder()
                .messageId(savedMessage.getId())
                .chatId(chat.getId())
                .senderId(senderId)
                .recipientId(receiver.getId())
                .content(savedMessage.getContent())
                .mediaFile(savedMessage.getMediaFile())
                .type(savedMessage.getType())
                .build());

        return savedMessage;
    }

    private String getSenderId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())) {
            return chat.getSender().getId();
        }
        return chat.getRecipient().getId();
    }

    private String getRecipientId(Chat chat, Authentication authentication) {
        if (chat.getSender().getId().equals(authentication.getName())) {
            return chat.getRecipient().getId();
        }
        return chat.getSender().getId();
    }
}