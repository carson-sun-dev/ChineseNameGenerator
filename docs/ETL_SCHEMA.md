# SQLite Schema and ETL Mapping

## Tables

### `character_entry`
- `hanzi` (TEXT, unique): base character.
- `pinyin` (TEXT): normalized lowercase pinyin token without tone marks.
- `tone` (INTEGER): tone number 1..4/5.
- `meaning` (TEXT): short semantic explanation.
- `style_tags` (TEXT): comma-separated normalized tags (`elegant,scholarly`).
- `frequency_rank` (INTEGER): lower rank means more common.

### `favorites`
- `english_name` (TEXT): original user input.
- `full_name` (TEXT): generated surname+given name.
- `pinyin` (TEXT): generated pinyin output.
- `score` (REAL): final score from the ranking model.
- `created_at` (TEXT): insertion timestamp.

## ETL mapping examples

- `CC-CEDICT.simplified` -> `character_entry.hanzi`
- `CC-CEDICT.pinyin` -> split into `pinyin` + `tone`
- `CC-CEDICT.english` -> `meaning` (first gloss or merged concise gloss)
- poetry/theme metadata -> `style_tags`
- frequency corpus rank -> `frequency_rank`

## ETL workflow

1. Parse source JSON/TXT.
2. Normalize pinyin and style tags.
3. Validate mandatory fields.
4. Upsert rows using JDBC transaction.
