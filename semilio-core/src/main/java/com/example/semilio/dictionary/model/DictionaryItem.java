package com.example.semilio.dictionary.model;

public interface DictionaryItem {
    Long getId();
    String getName();
    String getSlug();
    Integer getSortOrder();

    default String getHexCode() {
        return null;
    }

    default String getType() {
        return null;
    }
}