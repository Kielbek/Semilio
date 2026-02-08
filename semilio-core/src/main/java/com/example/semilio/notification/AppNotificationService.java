package com.example.semilio.notification;

import com.example.semilio.notification.response.NotificationResponse;

public interface AppNotificationService {

    void send(String recipientId, NotificationResponse notificationDto);

}
