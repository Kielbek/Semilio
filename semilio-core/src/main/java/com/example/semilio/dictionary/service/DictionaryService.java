package com.example.semilio.dictionary.service;

import com.example.semilio.dictionary.response.DictionaryResponse;

import java.util.List;
import java.util.Map;

public interface DictionaryService {

    List<DictionaryResponse> getActiveBrands();

    List<DictionaryResponse> getActiveColors();

    Map<String, List<DictionaryResponse>> getActiveSizes();
}
