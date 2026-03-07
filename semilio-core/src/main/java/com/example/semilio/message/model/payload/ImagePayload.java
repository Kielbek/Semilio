package com.example.semilio.message.model.payload;

public record ImagePayload(
        String url,
        int width,
        int height,
        String mimeType
) implements MessagePayload {}