package dev.carson.chinesename.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneticMapperTest {
    @Test
    void mapsKnownNameWithFallbackSafety() {
        PhoneticMapper mapper = new PhoneticMapper();
        List<String> tokens = mapper.mapEnglishToPinyinTokens("Alice");
        assertFalse(tokens.isEmpty());
        assertTrue(tokens.stream().noneMatch(String::isBlank));
    }
}
