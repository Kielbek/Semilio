package com.example.semilio.dictionary.controller;

import com.example.semilio.dictionary.response.DictionaryResponse;
import com.example.semilio.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/v1/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictionaryService dictionaryService;

    @GetMapping("/public/init")
    public ResponseEntity<Map<String, Object>> getInitConfiguration() {
        Map<String, Object> config = Map.of(
                "brands", dictionaryService.getActiveBrands(),
                "colors", dictionaryService.getActiveColors(),
                "sizes", dictionaryService.getActiveSizes()
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(config);
    }
}