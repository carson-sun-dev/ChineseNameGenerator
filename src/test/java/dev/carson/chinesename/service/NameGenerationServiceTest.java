package dev.carson.chinesename.service;

import dev.carson.chinesename.model.CharacterEntry;
import dev.carson.chinesename.model.NameCandidate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NameGenerationServiceTest {
    @Test
    void returnsTopKSortedCandidates() {
        NameGenerationService service = new NameGenerationService(new PhoneticMapper());
        List<CharacterEntry> pool = List.of(
                new CharacterEntry("艾", "ai", 4, "elegant herb", "graceful tone", Set.of("elegant"), Set.of("female", "neutral"), 0.2, 0.8, 100),
                new CharacterEntry("丽", "li", 4, "beautiful", "beauty meaning", Set.of("elegant"), Set.of("female"), 0.1, 0.9, 90),
                new CharacterEntry("思", "si", 1, "thoughtful", "neutral scholar", Set.of("scholarly"), Set.of("neutral"), 0.5, 0.5, 80)
        );
        List<NameCandidate> out = service.generate("Alice", "", "孙", "elegant", "Female", pool, 2);
        assertEquals(2, out.size());
        assertTrue(out.get(0).score() >= out.get(1).score());
        assertFalse(out.get(0).fullName().isBlank());
    }

    @Test
    void generatesChineseSurnameFromEnglishInput() {
        NameGenerationService service = new NameGenerationService(new PhoneticMapper());
        List<CharacterEntry> pool = List.of(
                new CharacterEntry("马", "ma", 3, "horse", "bold note", Set.of("bold"), Set.of("male"), 0.8, 0.2, 90),
                new CharacterEntry("思", "si", 1, "thoughtful", "neutral note", Set.of("scholarly"), Set.of("neutral"), 0.5, 0.5, 80),
                new CharacterEntry("丽", "li", 4, "beautiful", "beauty note", Set.of("elegant"), Set.of("female"), 0.1, 0.9, 70)
        );
        List<NameCandidate> out = service.generate("Alice", "", "miller", "bold", "Male", pool, 1);
        assertEquals("米", out.get(0).surname());
    }

    @Test
    void maleModeFiltersStrongFemaleCharacter() {
        NameGenerationService service = new NameGenerationService(new PhoneticMapper());
        List<CharacterEntry> pool = List.of(
                new CharacterEntry("伟", "wei", 3, "great", "male note", Set.of("bold"), Set.of("male"), 0.9, 0.1, 35),
                new CharacterEntry("强", "qiang", 2, "strong", "male note", Set.of("bold"), Set.of("male"), 0.9, 0.1, 45),
                new CharacterEntry("丽", "li", 4, "beautiful", "female note", Set.of("elegant"), Set.of("female"), 0.1, 0.9, 50)
        );
        List<NameCandidate> out = service.generate("Alex", "", "smith", "bold", "Male", pool, 3);
        assertFalse(out.isEmpty());
        assertTrue(out.stream().allMatch(c -> !c.givenName().contains("丽")));
        assertNotEquals("孙", out.get(0).surname());
    }
}
