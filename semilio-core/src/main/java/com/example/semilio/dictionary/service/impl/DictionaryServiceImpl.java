package com.example.semilio.dictionary.service.impl;

import com.example.semilio.dictionary.mapper.DictionaryMapper;
import com.example.semilio.dictionary.repository.BrandRepository;
import com.example.semilio.dictionary.repository.ColorRepository;
import com.example.semilio.dictionary.repository.SizeRepository;
import com.example.semilio.dictionary.response.DictionaryResponse;
import com.example.semilio.dictionary.service.DictionaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {

    private final BrandRepository brandRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final DictionaryMapper dictionaryMapper;

    @Override
    @Cacheable(value = "dictionaries", key = "'brands'", sync = true)
    @Transactional(readOnly = true)
    public List<DictionaryResponse> getActiveBrands() {
        log.info("Cache miss for 'brands'. Fetching active brands from the database.");
        return dictionaryMapper.toResponseList(brandRepository.findAllByActiveTrueOrderByNameAsc());
    }

    @Override
    @Cacheable(value = "dictionaries", key = "'colors'", sync = true)
    @Transactional(readOnly = true)
    public List<DictionaryResponse> getActiveColors() {
        log.info("Cache miss for 'colors'. Fetching active colors from the database.");
        return dictionaryMapper.toResponseList(colorRepository.findAllByActiveTrueOrderBySortOrderAsc());
    }

    @Override
    @Cacheable(value = "dictionaries", key = "'sizes_grouped'", sync = true)
    @Transactional(readOnly = true)
    public Map<String, List<DictionaryResponse>> getActiveSizes() {
        log.info("Cache miss for 'sizes_grouped'. Fetching and grouping sizes.");

        return sizeRepository.findAllByActiveTrueOrderBySortOrderAsc().stream()
                .map(dictionaryMapper::toResponse)
                .collect(Collectors.groupingBy(
                        DictionaryResponse::type,
                        Collectors.toList()
                ));
    }
}