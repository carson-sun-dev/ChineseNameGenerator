package edu.cs6103.chinesename.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseManager {
    private final String jdbcUrl;

    public DatabaseManager(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    public void initSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(readResource("/schema.sql"));
            ensureCharacterEntryColumns(conn);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize schema", e);
        }
    }

    private void ensureCharacterEntryColumns(Connection conn) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(character_entry)")) {
            while (rs.next()) {
                columns.add(rs.getString("name"));
            }
        }
        if (!columns.contains("gender_tags")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE character_entry ADD COLUMN gender_tags TEXT NOT NULL DEFAULT 'neutral'");
            }
        }
        if (!columns.contains("source_note")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE character_entry ADD COLUMN source_note TEXT NOT NULL DEFAULT ''");
            }
        }
        if (!columns.contains("male_ratio")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE character_entry ADD COLUMN male_ratio REAL NOT NULL DEFAULT 0.5");
            }
        }
        if (!columns.contains("female_ratio")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE character_entry ADD COLUMN female_ratio REAL NOT NULL DEFAULT 0.5");
            }
        }
    }

    private String readResource(String path) {
        InputStream in = DatabaseManager.class.getResourceAsStream(path);
        if (in == null) {
            throw new IllegalArgumentException("Missing resource: " + path);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read resource " + path, e);
        }
    }
}
