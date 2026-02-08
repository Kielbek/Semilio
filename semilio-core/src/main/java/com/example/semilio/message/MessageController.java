package com.example.semilio.message;

import com.example.semilio.message.request.MessageRequest;
import com.example.semilio.message.response.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/{chatId}")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable String chatId,
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

    @PostMapping(value = "/{chatId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> uploadMedia(
            @PathVariable String chatId,
            @RequestPart("file") MultipartFile file,
            Authentication principal
    ) {
        return ResponseEntity.ok(messageService.uploadMediaMessage(chatId, file, principal));
    }
}
