package com.example.semilio.comon.dictionary;

import org.springframework.stereotype.Service;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DictionaryService {

    public List<CountryDTO> getCountries(String languageCode) {
        Locale displayLocale = new Locale(languageCode != null ? languageCode : "pl");

        return Arrays.stream(Locale.getISOCountries())
                .map(countryCode -> {
                    Locale countryLocale = new Locale("", countryCode);
                    String countryName = countryLocale.getDisplayCountry(displayLocale);
                    return new CountryDTO(countryCode, countryName);
                })
                .filter(dto -> !dto.getName().isEmpty())
                .sorted(Comparator.comparing(CountryDTO::getName, Collator.getInstance(displayLocale)))
                .collect(Collectors.toList());
    }

    public String getCountryNameByCode(String countryCode, String languageCode) {
        if (countryCode == null || countryCode.isEmpty()) {
            return "";
        }

        Locale displayLocale = new Locale(languageCode != null ? languageCode : "pl");
        Locale countryLocale = new Locale("", countryCode);

        return countryLocale.getDisplayCountry(displayLocale);
    }
}