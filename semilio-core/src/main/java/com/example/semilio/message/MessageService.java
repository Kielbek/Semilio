package com.example.semilio.message;

import com.example.semilio.message.request.MessageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

    void saveMessage(MessageRequest messageRequest);

    void setMessagesToSeen(String chatId, Authentication authentication);
}
