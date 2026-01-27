package com.example.semilio.notification;

import lombok.Getter;

public enum EmailTemplates {

    RESET_PASSWORD("reset-password.html", "Reset password"),
    ;

    @Getter
    private final String template;
    @Getter
    private final String subject;

    EmailTemplates(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }
}
