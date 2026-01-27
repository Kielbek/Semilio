package com.example.semilio.chat;

import com.example.semilio.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<String> createChat(
            Authentication principal,
            @RequestParam(name = "receiver-id") String receiverId
    ) {
        final String chatId = chatService.createChat(principal, receiverId);

        return ResponseEntity.ok(chatId);
    }

    @GetMapping
    public ResponseEntity<Page<ChatResponse>> getChatsByReceiver(
            Authentication principal,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(chatService.getChatsByReceiverId(principal, pageable));
    }
}