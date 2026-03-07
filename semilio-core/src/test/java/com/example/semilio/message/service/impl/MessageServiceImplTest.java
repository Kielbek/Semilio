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
import com.example.semilio.message.model.payload.ProposalPayload;
import com.example.semilio.message.model.payload.TextPayload;
import com.example.semilio.message.repository.MessageRepository;
import com.example.semilio.message.request.MessageRequest;
import com.example.semilio.message.request.SendProposalRequest;
import com.example.semilio.message.request.UpdateProposalStatusRequest;
import com.example.semilio.message.response.MessageResponse;
import com.example.semilio.notification.push.PushService;
import com.example.semilio.notification.push.PushResponse;
import com.example.semilio.product.model.Price;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.repository.ProductRepository;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock private MessageRepository messageRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ChatService chatService;
    @Mock private MessageMapper mapper;
    @Mock private SecurityService securityService;
    @Mock private PushService notificationService;
    @Mock private ImageService imageService;
    @Mock private FileValidator fileValidator;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Nested
    @DisplayName("Send Message Tests")
    class SendMessageTests {

        @Test
        @DisplayName("Should send text message successfully when chat id is provided")
        void shouldSendMessageSuccessfullyWithChatId() {
            UUID senderId = UUID.randomUUID();
            UUID buyerId = senderId;
            UUID sellerId = UUID.randomUUID();
            Long chatId = 1L;
            Authentication principal = mock(Authentication.class);
            MessageRequest request = new MessageRequest(chatId, null, "Hello");

            User sender = new User();
            sender.setId(senderId);

            Chat chat = new Chat();
            chat.setId(chatId);
            chat.setBuyerId(buyerId);
            chat.setSellerId(sellerId);

            Message savedMessage = Message.builder().id(100L).payload(new TextPayload("Hello")).build();
            MessageResponse expectedResponse = MessageResponse.builder().build();

            given(securityService.getCurrentUser(principal)).willReturn(sender);
            given(chatRepository.findById(chatId)).willReturn(Optional.of(chat));
            given(messageRepository.save(any(Message.class))).willReturn(savedMessage);
            given(mapper.toMessageResponse(savedMessage)).willReturn(expectedResponse);

            MessageResponse result = messageService.sendMessage(request, principal);

            assertThat(result).isEqualTo(expectedResponse);
            then(chatService).should(times(1)).validateChatAccess(chat, senderId);
            then(chatRepository).should(times(1)).updateStatsOnBuyerReply(chatId, 100L);
            then(notificationService).should(times(1)).send(eq(sellerId.toString()), any(PushResponse.class));
        }

        @Test
        @DisplayName("Should send text message successfully when product id is provided and sender is seller")
        void shouldSendMessageSuccessfullyWithProductId() {
            UUID senderId = UUID.randomUUID();
            UUID buyerId = UUID.randomUUID();
            UUID sellerId = senderId;
            UUID productId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            MessageRequest request = new MessageRequest( null, productId, "Hello");

            User sender = new User();
            sender.setId(senderId);

            Product product = new Product();
            product.setId(productId);

            Chat chat = new Chat();
            chat.setId(2L);
            chat.setBuyerId(buyerId);
            chat.setSellerId(sellerId);

            Message savedMessage = Message.builder().id(101L).payload(new TextPayload("Hello")).build();

            given(securityService.getCurrentUser(principal)).willReturn(sender);
            given(productRepository.findByIdWithUser(productId)).willReturn(Optional.of(product));
            given(chatService.createChat(senderId, product)).willReturn(chat);
            given(messageRepository.save(any(Message.class))).willReturn(savedMessage);

            messageService.sendMessage(request, principal);

            then(chatService).should(times(1)).validateChatAccess(chat, senderId);
            then(chatRepository).should(times(1)).updateStatsOnSellerReply(2L, 101L);
            then(notificationService).should(times(1)).send(eq(buyerId.toString()), any(PushResponse.class));
        }

        @Test
        @DisplayName("Should throw INVALID_CHAT_REQUEST when both chat id and product id are null")
        void shouldThrowExceptionWhenBothIdsAreNull() {
            UUID senderId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            MessageRequest request = new MessageRequest(null, null, "Hello");

            User sender = new User();
            sender.setId(senderId);

            given(securityService.getCurrentUser(principal)).willReturn(sender);

            assertThatThrownBy(() -> messageService.sendMessage(request, principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CHAT_REQUEST);
        }
    }

    @Nested
    @DisplayName("Get Messages Tests")
    class GetMessagesTests {

        @Test
        @DisplayName("Should return page of mapped messages")
        void shouldReturnPageOfMessages() {
            Long chatId = 1L;
            UUID currentUserId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            Pageable pageable = PageRequest.of(0, 10);

            Chat chat = new Chat();
            Message message = Message.builder().build();
            Page<Message> messagePage = new PageImpl<>(List.of(message));
            MessageResponse response = MessageResponse.builder().build();

            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);
            given(chatService.loadChatByIdWithDetails(chatId)).willReturn(chat);
            given(messageRepository.findByChatId(chatId, pageable)).willReturn(messagePage);
            given(mapper.toMessageResponse(message)).willReturn(response);

            Page<MessageResponse> result = messageService.getMessages(chatId, pageable, principal);

            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(response);
            then(chatService).should(times(1)).validateChatAccess(chat, currentUserId);
        }
    }

    @Nested
    @DisplayName("Proposal Tests")
    class ProposalTests {

        @Test
        @DisplayName("Should send proposal with SUGGESTED status if sender is seller")
        void shouldSendProposalAsSeller() {
            UUID senderId = UUID.randomUUID();
            Long chatId = 1L;
            UUID productId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            SendProposalRequest request = new SendProposalRequest(chatId, productId, new BigDecimal("100.00"));

            User sender = new User();
            sender.setId(senderId);

            Chat chat = new Chat();
            chat.setId(chatId);
            chat.setSellerId(senderId);
            chat.setBuyerId(UUID.randomUUID());
            chat.setProductId(productId);

            Product product = new Product();
            product.setPrice(Price.builder().amount(new BigDecimal("150.00")).currencyCode("PLN").build());

            Message savedMessage = Message.builder().id(10L).payload(new ProposalPayload(new BigDecimal("100.00"), "PLN", new BigDecimal("150.00"), ProposalStatus.SUGGESTED)).build();

            given(securityService.getCurrentUser(principal)).willReturn(sender);
            given(chatRepository.findById(chatId)).willReturn(Optional.of(chat));
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(messageRepository.save(any(Message.class))).willReturn(savedMessage);

            messageService.sendProposal(request, principal);

            then(messageRepository).should(times(1)).save(org.mockito.ArgumentMatchers.argThat(msg ->
                    msg.getType() == MessageType.PROPOSAL &&
                            ((ProposalPayload) msg.getPayload()).status() == ProposalStatus.SUGGESTED
            ));
        }

        @Test
        @DisplayName("Should update proposal status successfully when user is seller and status is pending")
        void shouldUpdateProposalStatusSuccessfully() {
            Long messageId = 1L;
            UUID sellerId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            UpdateProposalStatusRequest request = new UpdateProposalStatusRequest(ProposalStatus.ACCEPTED);

            Chat chat = new Chat();
            chat.setId(2L);
            chat.setSellerId(sellerId);
            chat.setBuyerId(UUID.randomUUID());

            Message message = Message.builder()
                    .id(messageId)
                    .chatId(2L)
                    .payload(new ProposalPayload(new BigDecimal("100"), "PLN", new BigDecimal("150"), ProposalStatus.PENDING))
                    .build();

            given(securityService.getCurrentUserId(principal)).willReturn(sellerId);
            given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
            given(chatRepository.findById(2L)).willReturn(Optional.of(chat));
            given(messageRepository.save(any(Message.class))).willReturn(message);

            messageService.updateProposalStatus(messageId, request, principal);

            assertThat(((ProposalPayload) message.getPayload()).status()).isEqualTo(ProposalStatus.ACCEPTED);
            then(messageRepository).should(times(1)).save(message);
            then(chatRepository).should(times(1)).save(chat);
            then(notificationService).should(times(1)).send(anyString(), any(PushResponse.class));
        }

        @Test
        @DisplayName("Should throw ACCESS_DENIED when non-seller tries to update proposal")
        void shouldThrowAccessDeniedWhenNotSeller() {
            Long messageId = 1L;
            UUID buyerId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            UpdateProposalStatusRequest request = new UpdateProposalStatusRequest(ProposalStatus.ACCEPTED);

            Chat chat = new Chat();
            chat.setSellerId(sellerId);

            Message message = Message.builder().chatId(2L).build();

            given(securityService.getCurrentUserId(principal)).willReturn(buyerId);
            given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
            given(chatRepository.findById(message.getChatId())).willReturn(Optional.of(chat));

            assertThatThrownBy(() -> messageService.updateProposalStatus(messageId, request, principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("Should throw INVALID_MESSAGE_TYPE when updating non-proposal message")
        void shouldThrowInvalidMessageType() {
            Long messageId = 1L;
            UUID sellerId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            UpdateProposalStatusRequest request = new UpdateProposalStatusRequest(ProposalStatus.ACCEPTED);

            Chat chat = new Chat();
            chat.setSellerId(sellerId);

            Message message = Message.builder().chatId(2L).payload(new TextPayload("Not a proposal")).build();

            given(securityService.getCurrentUserId(principal)).willReturn(sellerId);
            given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
            given(chatRepository.findById(message.getChatId())).willReturn(Optional.of(chat));

            assertThatThrownBy(() -> messageService.updateProposalStatus(messageId, request, principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_MESSAGE_TYPE);
        }

        @Test
        @DisplayName("Should throw PROPOSAL_ALREADY_PROCESSED when status is already accepted")
        void shouldThrowProposalAlreadyProcessed() {
            Long messageId = 1L;
            UUID sellerId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            UpdateProposalStatusRequest request = new UpdateProposalStatusRequest(ProposalStatus.REJECTED);

            Chat chat = new Chat();
            chat.setSellerId(sellerId);

            Message message = Message.builder().chatId(2L).payload(new ProposalPayload(BigDecimal.TEN, "PLN", BigDecimal.TEN, ProposalStatus.ACCEPTED)).build();

            given(securityService.getCurrentUserId(principal)).willReturn(sellerId);
            given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
            given(chatRepository.findById(message.getChatId())).willReturn(Optional.of(chat));

            assertThatThrownBy(() -> messageService.updateProposalStatus(messageId, request, principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROPOSAL_ALREADY_PROCESSED);
        }
    }

    @Nested
    @DisplayName("Upload Media Tests")
    class UploadMediaTests {

        @Test
        @DisplayName("Should upload image message successfully")
        void shouldUploadMediaMessageSuccessfully() {
            Long chatId = 1L;
            UUID senderId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);
            MultipartFile file = mock(MultipartFile.class);

            User sender = new User();
            sender.setId(senderId);

            Chat chat = new Chat();
            chat.setId(chatId);
            chat.setBuyerId(senderId);
            chat.setSellerId(UUID.randomUUID());

            Image image = new Image();
            image.setUrl("http://example.com/img.jpg");
            image.setWidth(800);
            image.setHeight(600);

            Message savedMessage = Message.builder().id(99L).payload(new ImagePayload("url", 1, 1, "image/jpeg")).build();

            given(securityService.getCurrentUser(principal)).willReturn(sender);
            given(chatService.loadChatByIdWithDetails(chatId)).willReturn(chat);
            given(file.getContentType()).willReturn("image/jpeg");
            given(imageService.createImage(file, "message")).willReturn(image);
            given(messageRepository.save(any(Message.class))).willReturn(savedMessage);

            messageService.uploadMediaMessage(chatId, file, principal);

            then(fileValidator).should(times(1)).validateImage(file);
            then(chatService).should(times(1)).validateChatAccess(chat, senderId);
            then(messageRepository).should(times(1)).save(org.mockito.ArgumentMatchers.argThat(msg -> msg.getType() == MessageType.IMAGE));
        }
    }

    @Nested
    @DisplayName("Mark As Read Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should delegate to chat service to mark messages as read")
        void shouldMarkMessagesAsRead() {
            Long chatId = 1L;
            UUID currentUserId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);

            messageService.markMessagesAsRead(chatId, principal);

            then(chatService).should(times(1)).markChatAsRead(chatId, currentUserId);
        }
    }
}