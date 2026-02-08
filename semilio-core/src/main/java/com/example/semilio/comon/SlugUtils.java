package com.example.semilio.comon;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input, String uuid) {
        if (input == null || input.isEmpty()) return "";

        String nowhitespace = WHITESPACE.matcher(input.trim().toLowerCase(Locale.ROOT)).replaceAll("-");

        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");

        slug = slug.replace("ł", "l").replace("Ł", "l");

        slug = NONLATIN.matcher(slug).replaceAll("");

        String shortId = uuid.split("-")[0];

        return slug + "-" + shortId;
    }
}