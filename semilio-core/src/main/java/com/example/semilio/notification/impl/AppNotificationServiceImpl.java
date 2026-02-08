package com.example.semilio.notification.impl;

import com.example.semilio.notification.AppNotificationService;
import com.example.semilio.notification.response.NotificationResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppNotificationServiceImpl implements AppNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void send(String recipientId, NotificationResponse notificationDto) {
        log.info("Sending notification {} to user {}", notificationDto.getType(), recipientId);

        messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/notifications",
                notificationDto
        );
    }


    private void saveToDatabase(String recipientId, NotificationResponse dto) {
        // Tu mapujesz DTO na Encję i robisz repository.save(...)
    }
}
