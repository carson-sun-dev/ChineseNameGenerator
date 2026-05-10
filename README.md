# ChineseName v1

A desktop **JavaFX** app that builds **Chinese surnames + two-character given names** from English pronunciation and style preferences, then ranks candidates by an internal score. Suitable for coursework or prototyping.

---

## Features

| Area | Behavior |
|------|----------|
| English input | **First name**, optional **Middle name**, **Last name** (English family name); generation runs only if at least one field is non-empty |
| Chinese name order | **Surname first**, then two given-name characters (`surname + givenName`); pinyin follows the same order |
| Style | Single choice (e.g. elegant, scholarly, bold); feeds into scoring |
| Gender | Neutral / Male / Female; strongly mismatched characters are filtered out |
| Results | Each row: **Chinese full name + pinyin**; English blurb with pronunciation hints and short glosses for the two given-name characters; **no numeric match score in the UI** (scores still used for ranking) |
| Persistence | On startup, loads `sample_characters.json` into **SQLite** (`chinesename.db`) |

---

## Requirements

- **JDK 17**
- **Maven 3.x**

---

## Running

Run Maven from the directory that contains **`pom.xml`** (in this repo, `ChineseNamev1/`):

```bash
cd ChineseNamev1

mvn test
mvn javafx:run
```

Main class: `edu.cs6103.chinesename.ui.MainApp`.

---

## Project layout

```
src/main/java/edu/cs6103/chinesename/
  ui/           MainApp — JavaFX UI
  service/      Generation, phonetic mapping, scoring, pronunciation hints, ETL
  db/           SQLite and character repository
  model/        CharacterEntry, NameCandidate
src/main/resources/
  sample_characters.json   Sample character data
  schema.sql               Table definitions
docs/                      ETL notes and data-source docs
```

---

## Scoring model (matches code)

`ScoringModel` combines factors as:

```
score = 0.50 × phonetic
      + 0.25 × style
      + 0.10 × rarity
      + 0.05  × toneHarmony
      + 0.10 × gender
      - penalties
```

| Factor | Role |
|--------|------|
| **phonetic** | How well mapped English chunks align with the two candidate syllables |
| **style** | Whether the chosen style tag appears on either character’s `styleTags` |
| **rarity** | Uses `frequencyRank` to avoid both ultra-common-only and overly obscure pairs |
| **toneHarmony** | Preference for smoother tone pairs across the two given-name characters |
| **gender** | Fit to the selected gender using per-character gender ratios |
| **penalties** | Reserved; currently zero in the implementation |

---

## Phonetic mapping (`PhoneticMapper`)

English input is normalized, segmented, and mapped to pinyin-like tokens. When a chunk does not match directly, the mapper tries in order:

1. Soft replacements (e.g. `th`→`s`, `v`→`w`, `x`→`ks`)
2. Consonant–vowel template matching
3. First-letter-style heuristics
4. Neutral fallback syllables (e.g. `an`, `en`, `li`, `si`)

**Note:** **pinyin4j** is on the classpath, but the main pipeline uses the custom `PhoneticMapper`; you can wire pinyin4j in for richer behavior if needed.

---

## Data and ETL

- On startup, `EtlImporter` reads **`sample_characters.json`** and upserts into `character_entry`.
- Field conventions are documented in `docs/ETL_SCHEMA.md`.
- For larger or replacement datasets, keep the JSON → Jackson → `CharacterRepository` path; respect licenses and attribution (see below).

### Suggested open datasets (bring your own import)

1. [chinese-poetry/chinese-poetry](https://github.com/chinese-poetry/chinese-poetry) (MIT) — cultural text and tag ideas  
2. [CC-CEDICT](https://cc-cedict.org/wiki/) — dictionary entries and pinyin reference  

**Principle:** External data is **input only**; mapping, scoring, and generation logic are original Java in this project. If you use third-party data in a report or release, cite it and follow its license.

---

## FAQ

- **Maven says there is no POM?**  
  Run commands inside `ChineseNamev1`, not the parent `ChineseName` folder (which has no `pom.xml`).

- **Reset the database?**  
  Delete `chinesename.db` in the working directory and restart the app to re-import the sample JSON.
