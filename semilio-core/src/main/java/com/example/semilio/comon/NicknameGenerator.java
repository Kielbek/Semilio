package com.example.semilio.comon;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Random;

@Service
public class NicknameGenerator {

    private final Random random = new Random();

    public String generateNickname(String firstName, String lastName) {
        String cleanFirst = normalize(firstName);
        String cleanLast = normalize(lastName);

        String part1 = getSafeSubstring(cleanFirst, 3);
        String part2 = getSafeSubstring(cleanLast, 3);

        int randomSuffix = 100 + random.nextInt(900);

        return String.format("%s%s%d", part1, part2, randomSuffix);
    }

    private String normalize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String result = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        return result.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private String getSafeSubstring(String str, int length) {
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length);
    }
}