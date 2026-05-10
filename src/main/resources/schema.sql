CREATE TABLE IF NOT EXISTS character_entry (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    hanzi TEXT NOT NULL UNIQUE,
    pinyin TEXT NOT NULL,
    tone INTEGER NOT NULL,
    meaning TEXT,
    source_note TEXT NOT NULL DEFAULT '',
    style_tags TEXT NOT NULL,
    gender_tags TEXT NOT NULL DEFAULT 'neutral',
    male_ratio REAL NOT NULL DEFAULT 0.5,
    female_ratio REAL NOT NULL DEFAULT 0.5,
    frequency_rank INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    english_name TEXT NOT NULL,
    full_name TEXT NOT NULL,
    pinyin TEXT NOT NULL,
    score REAL NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
