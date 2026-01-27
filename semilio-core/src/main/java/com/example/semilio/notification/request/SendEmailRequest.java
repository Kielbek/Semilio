package com.example.semilio.notification.request;

import com.example.semilio.notification.EmailTemplates;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.Map;

@Getter
public class SendEmailRequest {
    @Email(message = "Invalid email address format")
    @NotNull(message = "Recipient email address cannot be null")
    private String to;

    @NotNull(message = "Email template cannot be null")
    private EmailTemplates template;

    @NotNull(message = "Variables cannot be null")
    private Map<String, Object> variables;
}
