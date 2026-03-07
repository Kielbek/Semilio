package com.example.semilio.chat.servie.impl;

import com.example.semilio.chat.mapper.ChatMapper;
import com.example.semilio.chat.model.Chat;
import com.example.semilio.chat.repository.ChatRepository;
import com.example.semilio.chat.response.ChatDetailResponse;
import com.example.semilio.chat.response.ChatListResponse;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.product.model.Product;
import com.example.semilio.product.repository.ProductRepository;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.model.User;
import com.example.semilio.user.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock private ChatRepository chatRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private SecurityService securityService;
    @Mock private ChatMapper mapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Nested
    @DisplayName("Get Chats By Receiver Id Tests")
    class GetChatsByReceiverIdTests {

        @Test
        @DisplayName("Should return empty page when user has no chats")
        void shouldReturnEmptyPageWhenUserHasNoChats() {
            Authentication principal = mock(Authentication.class);
            Pageable pageable = PageRequest.of(0, 10);
            UUID userId = UUID.randomUUID();

            given(securityService.getCurrentUserId(principal)).willReturn(userId);
            given(chatRepository.findAllByBuyerIdOrSellerId(eq(userId), eq(userId), any(Pageable.class)))
                    .willReturn(Page.empty());

            Page<ChatListResponse> result = chatService.getChatsByReceiverId(principal, pageable);

            assertThat(result).isEmpty();
            then(productRepository).shouldHaveNoInteractions();
            then(userRepository).shouldHaveNoInteractions();
            then(mapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should return mapped page of chats when user has active chats")
        void shouldReturnMappedPageOfChats() {
            Authentication principal = mock(Authentication.class);
            Pageable pageable = PageRequest.of(0, 10);
            UUID currentUserId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            Chat chat = new Chat();
            chat.setProductId(productId);
            chat.setBuyerId(currentUserId);
            chat.setSellerId(otherUserId);
            Page<Chat> chatPage = new PageImpl<>(List.of(chat));

            Product product = new Product();
            product.setId(productId);

            User interlocutor = new User();
            interlocutor.setId(otherUserId);

            ChatListResponse expectedResponse = ChatListResponse.builder().build();

            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);
            given(chatRepository.findAllByBuyerIdOrSellerId(eq(currentUserId), eq(currentUserId), any(Pageable.class)))
                    .willReturn(chatPage);
            given(productRepository.findAllById(List.of(productId))).willReturn(List.of(product));
            given(userRepository.findAllById(List.of(otherUserId))).willReturn(List.of(interlocutor));
            given(mapper.toListResponse(chat, product, interlocutor, currentUserId)).willReturn(expectedResponse);

            Page<ChatListResponse> result = chatService.getChatsByReceiverId(principal, pageable);

            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(expectedResponse);
            then(productRepository).should(times(1)).findAllById(List.of(productId));
            then(userRepository).should(times(1)).findAllById(List.of(otherUserId));
            then(mapper).should(times(1)).toListResponse(chat, product, interlocutor, currentUserId);
        }
    }

    @Nested
    @DisplayName("Create Chat Tests")
    class CreateChatTests {

        @Test
        @DisplayName("Should create new chat when it does not exist")
        void shouldCreateNewChatWhenNotExists() {
            UUID buyerId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            User seller = new User();
            seller.setId(sellerId);
            Product product = new Product();
            product.setId(productId);
            product.setSeller(seller);

            Chat savedChat = new Chat();

            given(chatRepository.findByProductIdAndBuyerId(productId, buyerId)).willReturn(Optional.empty());
            given(chatRepository.save(any(Chat.class))).willReturn(savedChat);

            Chat result = chatService.createChat(buyerId, product);

            assertThat(result).isEqualTo(savedChat);
            then(chatRepository).should(times(1)).save(any(Chat.class));
        }

        @Test
        @DisplayName("Should return existing chat when it already exists")
        void shouldReturnExistingChat() {
            UUID buyerId = UUID.randomUUID();
            UUID sellerId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            User seller = new User();
            seller.setId(sellerId);
            Product product = new Product();
            product.setId(productId);
            product.setSeller(seller);

            Chat existingChat = new Chat();

            given(chatRepository.findByProductIdAndBuyerId(productId, buyerId)).willReturn(Optional.of(existingChat));

            Chat result = chatService.createChat(buyerId, product);

            assertThat(result).isEqualTo(existingChat);
            then(chatRepository).should(never()).save(any(Chat.class));
        }

        @Test
        @DisplayName("Should throw exception when buyer is the same as seller")
        void shouldThrowExceptionWhenBuyerIsSeller() {
            UUID userId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();

            User seller = new User();
            seller.setId(userId);
            Product product = new Product();
            product.setId(productId);
            product.setSeller(seller);

            assertThatThrownBy(() -> chatService.createChat(userId, product))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_ACCESS_DENIED);

            then(chatRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Get Chat By Id Tests")
    class GetChatsByIdTests {

        @Test
        @DisplayName("Should return chat detail response when valid")
        void shouldReturnChatDetailResponse() {
            Long chatId = 1L;
            UUID currentUserId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            Chat chat = new Chat();
            chat.setBuyerId(currentUserId);
            chat.setSellerId(UUID.randomUUID());
            chat.setProductId(productId);

            Product product = new Product();
            ChatDetailResponse expectedResponse = ChatDetailResponse.builder().build();

            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);
            given(chatRepository.findById(chatId)).willReturn(Optional.of(chat));
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(mapper.toDetailResponse(chat, product, currentUserId)).willReturn(expectedResponse);

            ChatDetailResponse result = chatService.getChatsById(chatId, principal);

            assertThat(result).isEqualTo(expectedResponse);
        }
    }

    @Nested
    @DisplayName("Get Chat By Product Id Tests")
    class GetChatsByProductIdTests {

        @Test
        @DisplayName("Should return chat detail when chat exists and product is found")
        void shouldReturnChatDetail() {
            UUID productId = UUID.randomUUID();
            UUID currentUserId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            Chat chat = new Chat();
            chat.setBuyerId(currentUserId);
            chat.setSellerId(UUID.randomUUID());

            Product product = new Product();
            ChatDetailResponse expectedResponse = ChatDetailResponse.builder().build();

            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);
            given(chatRepository.findByProductIdAndBuyerId(productId, currentUserId)).willReturn(Optional.of(chat));
            given(productRepository.findById(productId)).willReturn(Optional.of(product));
            given(mapper.toDetailResponse(chat, product, currentUserId)).willReturn(expectedResponse);

            ChatDetailResponse result = chatService.getChatsByProductId(productId, principal);

            assertThat(result).isEqualTo(expectedResponse);
        }

        @Test
        @DisplayName("Should return null when chat does not exist for product")
        void shouldReturnNullWhenChatNotFound() {
            UUID productId = UUID.randomUUID();
            UUID currentUserId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);
            given(chatRepository.findByProductIdAndBuyerId(productId, currentUserId)).willReturn(Optional.empty());

            ChatDetailResponse result = chatService.getChatsByProductId(productId, principal);

            assertThat(result).isNull();
            then(productRepository).shouldHaveNoInteractions();
            then(mapper).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenProductNotFound() {
            UUID productId = UUID.randomUUID();
            UUID currentUserId = UUID.randomUUID();
            Authentication principal = mock(Authentication.class);

            Chat chat = new Chat();
            chat.setBuyerId(currentUserId);
            chat.setSellerId(UUID.randomUUID());

            given(securityService.getCurrentUserId(principal)).willReturn(currentUserId);
            given(chatRepository.findByProductIdAndBuyerId(productId, currentUserId)).willReturn(Optional.of(chat));
            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.getChatsByProductId(productId, principal))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PRODUCT_NOT_FOUND);

            then(mapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("Validation and Utility Methods Tests")
    class ValidationAndUtilityTests {

        @Test
        @DisplayName("Should pass validation when user is buyer")
        void shouldPassValidationWhenBuyer() {
            Chat chat = new Chat();
            UUID userId = UUID.randomUUID();
            chat.setBuyerId(userId);
            chat.setSellerId(UUID.randomUUID());

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> chatService.validateChatAccess(chat, userId));
        }

        @Test
        @DisplayName("Should pass validation when user is seller")
        void shouldPassValidationWhenSeller() {
            Chat chat = new Chat();
            UUID userId = UUID.randomUUID();
            chat.setSellerId(userId);
            chat.setBuyerId(UUID.randomUUID());

            org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> chatService.validateChatAccess(chat, userId));
        }

        @Test
        @DisplayName("Should throw exception when user has no access")
        void shouldThrowExceptionWhenNoAccess() {
            Chat chat = new Chat();
            chat.setBuyerId(UUID.randomUUID());
            chat.setSellerId(UUID.randomUUID());
            UUID unauthorizedUserId = UUID.randomUUID();

            assertThatThrownBy(() -> chatService.validateChatAccess(chat, unauthorizedUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_ACCESS_DENIED);
        }

        @Test
        @DisplayName("Should throw exception when chat not found by id")
        void shouldThrowExceptionWhenChatNotFoundById() {
            Long chatId = 1L;
            given(chatRepository.findById(chatId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.loadChatByIdWithDetails(chatId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CHAT_NOT_FOUND);
        }

        @Test
        @DisplayName("Should reset unread count correctly")
        void shouldMarkChatAsRead() {
            Long chatId = 1L;
            UUID userId = UUID.randomUUID();

            chatService.markChatAsRead(chatId, userId);

            then(chatRepository).should(times(1)).resetUnreadCount(chatId, userId);
        }
    }
}