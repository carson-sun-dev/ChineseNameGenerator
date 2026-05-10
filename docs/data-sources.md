# Data Sources and Attribution

## Selected sources

### 1) chinese-poetry/chinese-poetry
- URL: https://github.com/chinese-poetry/chinese-poetry
- Planned usage: extract culturally positive phrases/themes and map to style tags.
- License: MIT (repository states MIT).
- Project action: include attribution in report and repository README.

### 2) CC-CEDICT
- URL: https://cc-cedict.org/wiki/
- Planned usage: dictionary pinyin + basic gloss for character/word augmentation.
- License: follow CC-CEDICT terms on download page and keep required notices.
- Project action: store source version + retrieval date in ETL metadata table (future extension).

### 3) Common surname references (mapping calibration)
- URL: https://en.wikipedia.org/wiki/List_of_common_Chinese_surnames
- URL: https://en.wikipedia.org/wiki/Chinese_surname
- Planned usage: calibrate high-frequency surname set and common romanization variants (Wang/Wong, Lee/Li, Zhang/Chang).
- License/use note: reference-only statistical guidance; do not copy proprietary compiled tables as-is.

### 4) Given-name gender tendency references
- URL: https://en.wikipedia.org/wiki/Chinese_given_name
- URL: https://www.yourchineseastrology.com/chinese-name/given-names/
- Planned usage: bootstrap male/female tendency priors (e.g., 伟/强/杰 vs 丽/娜/芳) for initial scoring ratios.
- License/use note: used for heuristic initialization only; production should be replaced with large open corpus statistics.

## Integrity boundary

- External data is only input data for ETL.
- Name ranking formula, fallback mapping, and UI behavior are original Java implementation.
