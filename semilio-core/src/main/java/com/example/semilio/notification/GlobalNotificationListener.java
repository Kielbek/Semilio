package com.example.semilio.notification;

import com.example.semilio.message.event.MessageSentEvent;
import com.example.semilio.notification.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalNotificationListener {

    private final AppNotificationService notificationService;

    @Async
    @EventListener
    public void handleMessageSent(MessageSentEvent event) {
        log.info("Handling MessageSentEvent for chat: {}", event.chatId());

        Map<String, Object> extraData = new HashMap<>();

        extraData.put("chatId", event.chatId());
        extraData.put("senderId", event.senderId());
        extraData.put("messageType", event.type() != null ? event.type() : "TEXT");

        if (event.mediaFile() != null) {
            extraData.put("media", event.mediaFile());
        }

        NotificationResponse notification = NotificationResponse.builder()
                .id(String.valueOf(event.messageId()))
                .type(NotificationType.CHAT_MESSAGE)
                .content(event.content())
                .targetUrl("/chats/" + event.chatId())
                .createdAt(LocalDateTime.now())
                .read(false)
                .data(extraData)
                .build();

        notificationService.send(event.recipientId(), notification);
    }
}