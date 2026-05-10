package dev.carson.chinesename.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhoneticMapper {
    private static final Map<String, String> PRIMARY = Map.ofEntries(
            Map.entry("a", "a"), Map.entry("al", "ai"), Map.entry("li", "li"),
            Map.entry("ce", "si"), Map.entry("ka", "ka"), Map.entry("shu", "shu"),
            Map.entry("sun", "sen"), Map.entry("ma", "ma"), Map.entry("han", "han")
    );

    public List<String> mapEnglishToPinyinTokens(String englishName) {
        String normalized = englishName.toLowerCase().replaceAll("[^a-z]", "");
        List<String> chunks = segment(normalized);
        List<String> mapped = new ArrayList<>();
        for (String c : chunks) {
            mapped.add(resolveChunk(c));
        }
        return mapped;
    }

    private List<String> segment(String text) {
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            int len = Math.min(3, text.length() - i);
            String chunk = text.substring(i, i + len);
            if (len >= 2 && PRIMARY.containsKey(chunk)) {
                out.add(chunk);
                i += len;
            } else {
                out.add(text.substring(i, i + 1));
                i += 1;
            }
        }
        return out;
    }

    private String resolveChunk(String chunk) {
        String direct = PRIMARY.get(chunk);
        if (direct != null) {
            return direct;
        }
        String softened = chunk.replace("th", "s").replace("v", "w").replace("x", "ks");
        if (PRIMARY.containsKey(softened)) {
            return PRIMARY.get(softened);
        }
        if (chunk.matches("[bcdfghjklmnpqrstvwxyz][aeiou]")) {
            return String.valueOf(chunk.charAt(0)) + chunk.charAt(1);
        }
        if (!chunk.isEmpty()) {
            char first = chunk.charAt(0);
            if ("aeiou".indexOf(first) >= 0) {
                return "an";
            }
            return switch (first) {
                case 'l' -> "li";
                case 's' -> "si";
                case 'm' -> "ma";
                default -> "en";
            };
        }
        return "en";
    }
}
