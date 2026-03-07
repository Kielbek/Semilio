package com.example.semilio.message.model.payload;

public record TextPayload(
        String text
) implements MessagePayload {}