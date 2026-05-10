package dev.carson.chinesename.service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PronunciationRenderer {
    private static final Map<String, String> ENGLISH_HINTS = new HashMap<>();

    static {
        ENGLISH_HINTS.put("ai", "eye");
        ENGLISH_HINTS.put("li", "lee");
        ENGLISH_HINTS.put("si", "suh");
        ENGLISH_HINTS.put("han", "hahn");
        ENGLISH_HINTS.put("ma", "mah");
        ENGLISH_HINTS.put("en", "uhn");
        ENGLISH_HINTS.put("an", "ahn");
    }

    public String renderEnglishHint(String pinyinLine) {
        if (pinyinLine == null || pinyinLine.isBlank()) {
            return "";
        }
        String[] parts = pinyinLine.trim().toLowerCase(Locale.ROOT).split("\\s+");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                out.append(" - ");
            }
            out.append(ENGLISH_HINTS.getOrDefault(parts[i], fallback(parts[i])));
        }
        return out.toString();
    }

    private String fallback(String pinyin) {
        if (pinyin.startsWith("x")) {
            return "sh + " + pinyin.substring(1);
        }
        if (pinyin.startsWith("q")) {
            return "ch + " + pinyin.substring(1);
        }
        return pinyin;
    }
}
