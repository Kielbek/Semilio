package com.example.semilio.dictionary.mapper;

import com.example.semilio.dictionary.response.DictionaryResponse;
import com.example.semilio.dictionary.model.DictionaryItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DictionaryMapper {
    public DictionaryResponse toResponse(DictionaryItem item) {
        if (item == null) {
            return null;
        }

        return new DictionaryResponse(
                item.getId(),
                item.getName(),
                item.getSlug(),
                item.getHexCode(),
                item.getType()
        );
    }

    public List<DictionaryResponse> toResponseList(List<? extends DictionaryItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}