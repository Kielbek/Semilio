package com.example.semilio.category.impl;

import com.example.semilio.category.Category;
import com.example.semilio.category.CategoryRepository;
import com.example.semilio.category.CategoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void loadCategories() {
        if (categoryRepository.count() != 0) {
            log.info("Categories already exist in the database. Skipping initialization.");
            return;
        }

        try {
            log.info("Starting to load categories from JSON file...");
            InputStream inputStream = TypeReference.class.getResourceAsStream("/categories.json");

            if (inputStream == null) {
                log.error("Could not find categories.json file in resources!");
                return;
            }

            List<Category> categories = objectMapper.readValue(inputStream, new TypeReference<List<Category>>(){});

            assignParents(categories, null);

            categoryRepository.saveAll(categories);

            log.info("--- Success! Categories have been loaded into the database. Total root categories: {} ---", categories.size());

        } catch (IOException e) {
            log.error("Error occurred while parsing or saving categories: {}", e.getMessage(), e);
        }
    }

    private void assignParents(List<Category> categories, Category parent) {
        if (categories == null) return;

        for (Category category : categories) {
            category.setParent(parent);
            assignParents(category.getSubcategories(), category);
        }
    }
}