package edu.cs6103.chinesename.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cs6103.chinesename.db.CharacterRepository;
import edu.cs6103.chinesename.model.CharacterEntry;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EtlImporter {
    private final CharacterRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EtlImporter(CharacterRepository repository) {
        this.repository = repository;
    }

    public void importSampleData() {
        try (InputStream in = EtlImporter.class.getResourceAsStream("/sample_characters.json")) {
            if (in == null) {
                throw new IllegalStateException("sample_characters.json not found");
            }
            List<Map<String, Object>> rows = objectMapper.readValue(in, new TypeReference<>() {
            });
            for (Map<String, Object> row : rows) {
                repository.upsert(toEntry(row));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to import sample data", e);
        }
    }

    private CharacterEntry toEntry(Map<String, Object> row) {
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) row.get("styleTags");
        @SuppressWarnings("unchecked")
        List<String> genders = (List<String>) row.getOrDefault("genderTags", List.of("neutral"));
        Set<String> normalizedTags = tags.stream()
                .map(t -> t.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> normalizedGenders = genders.stream()
                .map(g -> g.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return new CharacterEntry(
                (String) row.get("hanzi"),
                (String) row.get("pinyin"),
                (Integer) row.get("tone"),
                (String) row.get("meaning"),
                (String) row.getOrDefault("sourceNote", ""),
                normalizedTags,
                normalizedGenders,
                ((Number) row.getOrDefault("maleRatio", 0.5)).doubleValue(),
                ((Number) row.getOrDefault("femaleRatio", 0.5)).doubleValue(),
                (Integer) row.get("frequencyRank")
        );
    }
}
