package edu.cs6103.chinesename.db;

import edu.cs6103.chinesename.model.CharacterEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CharacterRepository {
    private final DatabaseManager db;

    public CharacterRepository(DatabaseManager db) {
        this.db = db;
    }

    public void upsert(CharacterEntry e) {
        String sql = """
                INSERT INTO character_entry (hanzi, pinyin, tone, meaning, source_note, style_tags, gender_tags, male_ratio, female_ratio, frequency_rank)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(hanzi) DO UPDATE SET
                    pinyin=excluded.pinyin,
                    tone=excluded.tone,
                    meaning=excluded.meaning,
                    source_note=excluded.source_note,
                    style_tags=excluded.style_tags,
                    gender_tags=excluded.gender_tags,
                    male_ratio=excluded.male_ratio,
                    female_ratio=excluded.female_ratio,
                    frequency_rank=excluded.frequency_rank
                """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.hanzi());
            ps.setString(2, e.pinyin());
            ps.setInt(3, e.tone());
            ps.setString(4, e.meaning());
            ps.setString(5, e.sourceNote());
            ps.setString(6, String.join(",", e.styleTags()));
            ps.setString(7, String.join(",", e.genderTags()));
            ps.setDouble(8, e.maleRatio());
            ps.setDouble(9, e.femaleRatio());
            ps.setInt(10, e.frequencyRank());
            ps.executeUpdate();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to upsert character", ex);
        }
    }

    public List<CharacterEntry> findAll() {
        String sql = "SELECT hanzi, pinyin, tone, meaning, source_note, style_tags, gender_tags, male_ratio, female_ratio, frequency_rank FROM character_entry";
        List<CharacterEntry> rows = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Set<String> tags = new HashSet<>();
                for (String t : rs.getString("style_tags").split(",")) {
                    if (!t.isBlank()) {
                        tags.add(t.trim().toLowerCase());
                    }
                }
                Set<String> genderTags = new HashSet<>();
                for (String g : rs.getString("gender_tags").split(",")) {
                    if (!g.isBlank()) {
                        genderTags.add(g.trim().toLowerCase());
                    }
                }
                rows.add(new CharacterEntry(
                        rs.getString("hanzi"),
                        rs.getString("pinyin"),
                        rs.getInt("tone"),
                        rs.getString("meaning"),
                        rs.getString("source_note"),
                        tags,
                        genderTags,
                        rs.getDouble("male_ratio"),
                        rs.getDouble("female_ratio"),
                        rs.getInt("frequency_rank")
                ));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to query characters", ex);
        }
        return rows;
    }
}
