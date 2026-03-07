package com.example.semilio.message.service;

import com.example.semilio.message.request.MessageRequest;
import com.example.semilio.message.request.SendProposalRequest;
import com.example.semilio.message.request.UpdateProposalStatusRequest;
import com.example.semilio.message.response.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

public interface MessageService {

    MessageResponse sendMessage(MessageRequest request, Authentication authentication);

    Page<MessageResponse> getMessages(Long chatId, Pageable pageable, Authentication principal);

    MessageResponse sendProposal(SendProposalRequest request, Authentication principal);

    MessageResponse updateProposalStatus(Long messageId, UpdateProposalStatusRequest request, Authentication principal);

    MessageResponse uploadMediaMessage(Long chatId, MultipartFile file, Authentication principal);

    void markMessagesAsRead(Long chatId, Authentication principal);
}
