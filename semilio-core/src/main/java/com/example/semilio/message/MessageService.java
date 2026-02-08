package com.example.semilio.message;

import com.example.semilio.message.request.MessageRequest;
import com.example.semilio.message.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

    MessageResponse sendMessage(MessageRequest request, Authentication authentication);

    Page<MessageResponse> getMessages(String chatId, Pageable pageable, Authentication principal);

    void setMessagesToSeen(String chatId, Authentication authentication);

    MessageResponse uploadMediaMessage(String chatId, MultipartFile file, Authentication principal);
}
