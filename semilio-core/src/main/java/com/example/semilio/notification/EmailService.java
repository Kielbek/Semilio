package com.example.semilio.notification;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface EmailService {
    void sendEmail(String to, EmailTemplates template, Map<String, Object> variables);
}
