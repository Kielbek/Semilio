package com.example.semilio.notification.push;

public interface PushService {

    void send(String recipientId, PushResponse notificationDto);

}
