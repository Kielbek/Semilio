package com.example.semilio.message.controller;

import com.example.semilio.message.request.MessageRequest;
import com.example.semilio.message.request.SendProposalRequest;
import com.example.semilio.message.request.UpdateProposalStatusRequest;
import com.example.semilio.message.response.MessageResponse;
import com.example.semilio.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{chatId}")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long chatId,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication principal

    ) {

        return ResponseEntity.ok(messageService.getMessages(chatId, pageable, principal));
    }

    @PostMapping()
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody MessageRequest request,
            Authentication principal
            ) {
        return ResponseEntity.ok(messageService.sendMessage(request, principal));
    }

    @PostMapping("/proposal")
    public ResponseEntity<MessageResponse> sendProposal(
            @Valid @RequestBody SendProposalRequest request,
            Authentication principal
    ) {
        return ResponseEntity.ok(messageService.sendProposal(request, principal));
    }

    @PatchMapping("/{messageId}/proposal-status")
    public ResponseEntity<MessageResponse> updateStatus(
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateProposalStatusRequest request,
            Authentication principal
    ) {
        return ResponseEntity.ok(messageService.updateProposalStatus(messageId, request, principal));
    }

    @PostMapping(value = "/{chatId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> uploadMedia(
            @PathVariable Long chatId,
            @RequestPart("file") MultipartFile file,
            Authentication principal
    ) {
        return ResponseEntity.ok(messageService.uploadMediaMessage(chatId, file, principal));
    }

    @PatchMapping("/{chatId}/read")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long chatId,
            Authentication principal) {

        messageService.markMessagesAsRead(chatId, principal);

        return ResponseEntity.noContent().build();
    }
}
