package com.example.semilio.dictionary.response;

public record DictionaryResponse(
        Long id,
        String name,
        String slug,
        String hexCode,
        String type
) {}