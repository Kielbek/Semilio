package com.example.semilio.message.service.impl;

import com.example.semilio.chat.model.Chat;
import com.example.semilio.chat.repository.ChatRepository;
import com.example.semilio.chat.servie.ChatService;
import com.example.semilio.comon.validation.FileValidator;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.image.model.Image;
import com.example.semilio.image.service.ImageService;
import com.example.semilio.message.enums.MessageType;
import com.example.semilio.message.enums.ProposalStatus;
import com.example.semilio.message.mapper.MessageMapper;
import com.example.semilio.message.model.Message;
import com.example.semilio.message.model.payload.ImagePayload;
import com.example.semilio.message.model.payload.MessagePayload;
import com.example.semilio.message.model.payload.ProposalPayload;
import com.example.semilio.message.model.payload.TextPayload;
import com.example.semilio.message.repository.MessageRepository;
import com.example.semilio.message.request.MessageRequest;
import com.example.semilio.message.request.SendProposalRequest;
import com.example.semilio.message.request.UpdateProposalStatusRequest;
import com.example.semilio.message.response.MessageResponse;
import com.example.semilio.message.service.MessageService;
import com.example.semilio.notification.push.PushService;
import com.example.semilio.notification.push.PushType;
import com.example.semilio.notification.push.PushResponse;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.repository.ProductRepository;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final ProductRepository productRepository;
    private final ChatService chatService;
    private final MessageMapper mapper;
    private final SecurityService securityService;
    private final PushService notificationService;
    private final ImageService imageService;
    private final FileValidator fileValidator;

    @Override
    @Transactional
    public MessageResponse sendMessage(MessageRequest request, Authentication principal) {
        User sender = securityService.getCurrentUser(principal);
        Chat chat = resolveChat(sender.getId(), request.chatId(), request.productId());
        chatService.validateChatAccess(chat, sender.getId());

        MessagePayload payload = new TextPayload(request.content());

        return createAndPublishMessage(chat, sender.getId(), MessageType.TEXT, payload);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long chatId, Pageable pageable, Authentication principal) {
        UUID currentUserId = securityService.getCurrentUserId(principal);
        Chat chat = chatService.loadChatByIdWithDetails(chatId);
        chatService.validateChatAccess(chat, currentUserId);

        return messageRepository.findByChatId(chatId, pageable).map(mapper::toMessageResponse);
    }

    @Override
    @Transactional
    public MessageResponse sendProposal(SendProposalRequest request, Authentication principal) {
        User sender = securityService.getCurrentUser(principal);
        Chat chat = resolveChat(sender.getId(), request.chatId(), request.productId());
        chatService.validateChatAccess(chat, sender.getId());

        boolean isOwner = sender.getId().equals(chat.getSellerId());
        ProposalStatus initialStatus = isOwner ? ProposalStatus.SUGGESTED : ProposalStatus.PENDING;

        Product product = productRepository.findById(chat.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        MessagePayload payload = new ProposalPayload(
                request.amount(),
                product.getPrice().getCurrencyCode(),
                product.getPrice().getAmount(),
                initialStatus
        );

        return createAndPublishMessage(chat, sender.getId(), MessageType.PROPOSAL, payload);
    }

    @Override
    @Transactional
    public MessageResponse updateProposalStatus(Long messageId, UpdateProposalStatusRequest request, Authentication principal) {
        UUID currentUserId = securityService.getCurrentUserId(principal);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MESSAGE_NOT_FOUND));

        Chat chat = chatRepository.findById(message.getChatId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_FOUND));

        ProposalPayload newPayload = calculateNewProposalPayload(request, message, chat, currentUserId);
        message.setPayload(newPayload);
        Message savedMessage = messageRepository.save(message);

        chatRepository.save(chat);

        sendThroughNotificationSystem(savedMessage, chat, currentUserId);

        return mapper.toMessageResponse(savedMessage);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long chatId, Authentication principal) {
        final UUID currentUserId = securityService.getCurrentUserId(principal);
        chatService.markChatAsRead(chatId, currentUserId);
    }

    @Override
    @Transactional
    public MessageResponse uploadMediaMessage(Long chatId, MultipartFile file, Authentication authentication) {
        fileValidator.validateImage(file);
        User sender = securityService.getCurrentUser(authentication);
        Chat chat = chatService.loadChatByIdWithDetails(chatId);
        chatService.validateChatAccess(chat, sender.getId());

        Image image = imageService.createImage(file, "message");
        MessagePayload payload = new ImagePayload(
                image.getUrl(),
                image.getWidth(),
                image.getHeight(),
                file.getContentType()
        );

        return createAndPublishMessage(chat, sender.getId(), MessageType.IMAGE, payload);
    }

    private MessageResponse createAndPublishMessage(Chat chat, UUID senderId, MessageType type, MessagePayload payload) {
        Message message = Message.builder()
                .chatId(chat.getId())
                .senderId(senderId)
                .type(type)
                .payload(payload)
                .build();

        Message savedMessage = messageRepository.save(message);

        boolean isSenderBuyer = senderId.equals(chat.getBuyerId());
        Long newSeq = savedMessage.getId();

        if (isSenderBuyer) {
            chatRepository.updateStatsOnBuyerReply(chat.getId(), newSeq);
        } else {
            chatRepository.updateStatsOnSellerReply(chat.getId(), newSeq);
        }

        sendThroughNotificationSystem(savedMessage, chat, senderId);

        return mapper.toMessageResponse(savedMessage);
    }

    private void sendThroughNotificationSystem(Message message, Chat chat, UUID senderId) {
        UUID receiverId = determineReceiverId(chat, senderId);
        MessageResponse messageDto = mapper.toMessageResponse(message);

        PushResponse notification = PushResponse.builder()
                .id(String.valueOf(message.getId()))
                .type(PushType.CHAT_MESSAGE)
                .title("Nowa wiadomość")
                .content(generatePreviewContent(message.getPayload()))
                .targetUrl("/chats/" + chat.getId())
                .createdAt(LocalDateTime.now())
                .data(messageDto)
                .build();

        notificationService.send(receiverId.toString(), notification);
    }

    private String generatePreviewContent(MessagePayload payload) {
        if (payload instanceof TextPayload text) {
            return text.text();
        } else if (payload instanceof ImagePayload) {
            return "Przesłano zdjęcie";
        } else if (payload instanceof ProposalPayload) {
            return "Złożono nową ofertę";
        }
        return "Nowa wiadomość";
    }

    private Chat resolveChat(UUID senderId, Long chatId, UUID productId) {
        if (chatId != null) {
            return chatRepository.findById(chatId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_NOT_FOUND));
        }
        if (productId != null) {
            Product product = productRepository.findByIdWithUser(productId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            return chatService.createChat(senderId, product);
        }
        throw new BusinessException(ErrorCode.INVALID_CHAT_REQUEST);
    }

    private UUID determineReceiverId(Chat chat, UUID senderId) {
        return senderId.equals(chat.getBuyerId()) ? chat.getSellerId() : chat.getBuyerId();
    }

    private ProposalPayload calculateNewProposalPayload(
            UpdateProposalStatusRequest request,
            Message message, Chat chat,
            UUID currentUserId) {

        if (!chat.getSellerId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (!(message.getPayload() instanceof ProposalPayload oldPayload)) {
            throw new BusinessException(ErrorCode.INVALID_MESSAGE_TYPE);
        }

        if (oldPayload.status() == ProposalStatus.ACCEPTED ||
                oldPayload.status() == ProposalStatus.REJECTED) {
            throw new BusinessException(ErrorCode.PROPOSAL_ALREADY_PROCESSED);
        }

        return new ProposalPayload(
                oldPayload.amount(),
                oldPayload.currency(),
                oldPayload.originalPrice(),
                request.newStatus()
        );
    }
}