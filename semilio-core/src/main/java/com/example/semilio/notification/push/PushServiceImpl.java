package com.example.semilio.notification.push;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushServiceImpl implements PushService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void send(String recipientId, PushResponse notificationDto) {
        log.info("Sending notification {} to user {}", notificationDto.type(), recipientId);

        messagingTemplate.convertAndSendToUser(
                recipientId,
                "/queue/notifications",
                notificationDto
        );
    }


    private void saveToDatabase(String recipientId, PushResponse dto) {
        // Tu mapujesz DTO na Encję i robisz repository.save(...)
    }
}
