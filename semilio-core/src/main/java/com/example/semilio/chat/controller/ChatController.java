package com.example.semilio.chat.controller;

import com.example.semilio.chat.response.ChatDetailResponse;
import com.example.semilio.chat.response.ChatListResponse;
import com.example.semilio.chat.servie.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<Page<ChatListResponse>> getChatsByReceiver(
            Authentication principal,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(chatService.getChatsByReceiverId(principal, pageable));
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDetailResponse> getChatsById(
            @PathVariable Long chatId,
            Authentication principal
            ) {
        return ResponseEntity.ok(chatService.getChatsById(chatId, principal));
    }


    @GetMapping("/product/{productId}")
    public ResponseEntity<ChatDetailResponse> getChatsByProductId(
            @PathVariable UUID productId,
            Authentication principal
    ) {
        return ResponseEntity.ok(chatService.getChatsByProductId(productId, principal));
    }

    @GetMapping("/{chatId}/summary")
    public ResponseEntity<ChatListResponse> getSingleChatSummary(
            @PathVariable Long chatId,
            Authentication principal
    ) {
        return ResponseEntity.ok(chatService.getSingleChatListResponse(chatId, principal));
    }
}