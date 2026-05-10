package dev.carson.chinesename.service;

import dev.carson.chinesename.model.CharacterEntry;
import dev.carson.chinesename.model.NameCandidate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NameGenerationService {
    private final PhoneticMapper mapper;
    private final PronunciationRenderer pronunciationRenderer = new PronunciationRenderer();
    private static final Map<String, SurnameMapping> SURNAME_MAPPINGS = Map.ofEntries(
            Map.entry("smith", new SurnameMapping("史", "shi")),
            Map.entry("johnson", new SurnameMapping("约", "yue")),
            Map.entry("williams", new SurnameMapping("威", "wei")),
            Map.entry("brown", new SurnameMapping("布", "bu")),
            Map.entry("jones", new SurnameMapping("琼", "qiong")),
            Map.entry("miller", new SurnameMapping("米", "mi")),
            Map.entry("davis", new SurnameMapping("戴", "dai")),
            Map.entry("garcia", new SurnameMapping("加", "jia")),
            Map.entry("martin", new SurnameMapping("马", "ma")),
            Map.entry("lee", new SurnameMapping("李", "li")),
            Map.entry("li", new SurnameMapping("李", "li")),
            Map.entry("wang", new SurnameMapping("王", "wang")),
            Map.entry("zhang", new SurnameMapping("张", "zhang")),
            Map.entry("chen", new SurnameMapping("陈", "chen")),
            Map.entry("liu", new SurnameMapping("刘", "liu")),
            Map.entry("zhao", new SurnameMapping("赵", "zhao"))
    );

    public NameGenerationService(PhoneticMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * @param firstName   English given name (first)
     * @param middleName  optional English middle name; only used for phonetic mapping
     * @param lastName    English family name; mapped to Chinese surname (same role as the former surname field)
     */
    public List<NameCandidate> generate(
            String firstName,
            String middleName,
            String lastName,
            String preferredStyle,
            String targetGender,
            List<CharacterEntry> characterPool,
            int topK
    ) {
        String englishGiven = composeGivenNameForPhonetics(firstName, middleName);
        List<String> tokens = mapper.mapEnglishToPinyinTokens(englishGiven);
        SurnameMapping surnameMapping = resolveSurname(lastName, characterPool);
        String resolvedSurname = surnameMapping.hanzi();
        String surnamePinyin = surnameMapping.pinyin();
        List<NameCandidate> out = new ArrayList<>();
        for (CharacterEntry first : characterPool) {
            for (CharacterEntry second : characterPool) {
                if (first.hanzi().equals(second.hanzi())) {
                    continue;
                }
                if (isHardGenderMismatch(targetGender, first, second)) {
                    continue;
                }
                double phonetic = phoneticScore(tokens, List.of(first.pinyin(), second.pinyin()));
                double style = styleScore(preferredStyle, first.styleTags(), second.styleTags());
                double rarity = rarityScore(first.frequencyRank(), second.frequencyRank());
                double toneHarmony = toneHarmony(first.tone(), second.tone());
                double gender = genderScore(targetGender, first, second);
                double penalties = 0.0;
                double total = ScoringModel.score(phonetic, style, rarity, toneHarmony, gender, penalties);
                String pinyinLine = (surnamePinyin + " " + first.pinyin() + " " + second.pinyin()).trim();
                String pronunciationHint = pronunciationRenderer.renderEnglishHint(pinyinLine);
                String explanation = String.format(Locale.ROOT,
                        "Rough pronunciation for English speakers: %s. "
                                + "The given-name characters suggest %s combined with %s.",
                        pronunciationHint, first.meaning(), second.meaning());
                out.add(new NameCandidate(
                        resolvedSurname,
                        first.hanzi() + second.hanzi(),
                        pinyinLine,
                        total,
                        explanation
                ));
            }
        }
        out.sort(Comparator.comparingDouble(NameCandidate::score).reversed());
        return out.stream().limit(topK).toList();
    }

    /** Joins first + optional middle for mapping to the two-character given name; family name is excluded. */
    static String composeGivenNameForPhonetics(String firstName, String middleName) {
        String f = firstName == null ? "" : firstName.trim();
        String m = middleName == null ? "" : middleName.trim();
        if (f.isEmpty() && m.isEmpty()) {
            return "";
        }
        if (m.isEmpty()) {
            return f;
        }
        if (f.isEmpty()) {
            return m;
        }
        return f + " " + m;
    }

    private double phoneticScore(List<String> inputTokens, List<String> candidateTokens) {
        double sum = 0;
        int pairs = Math.min(inputTokens.size(), candidateTokens.size());
        if (pairs == 0) {
            return 0.2;
        }
        for (int i = 0; i < pairs; i++) {
            String in = inputTokens.get(i);
            String cd = candidateTokens.get(i);
            if (in.equals(cd)) {
                sum += 1.0;
            } else if (in.charAt(0) == cd.charAt(0)) {
                sum += 0.65;
            } else {
                sum += 0.25;
            }
        }
        return sum / pairs;
    }

    private double styleScore(String preferred, Set<String> firstTags, Set<String> secondTags) {
        if (preferred == null || preferred.isBlank()) {
            return 0.6;
        }
        String tag = preferred.trim().toLowerCase(Locale.ROOT);
        if (firstTags.contains(tag) || secondTags.contains(tag)) {
            return 1.0;
        }
        return 0.0;
    }

    private double rarityScore(int firstRank, int secondRank) {
        double avg = (firstRank + secondRank) / 2.0;
        return Math.max(0.2, Math.min(1.0, 1.0 - (avg / 200.0)));
    }

    private double toneHarmony(int t1, int t2) {
        if (t1 == t2) {
            return 0.55;
        }
        if ((t1 == 1 && t2 == 4) || (t1 == 4 && t2 == 1)) {
            return 0.95;
        }
        return 0.75;
    }

    private double genderScore(String targetGender, CharacterEntry first, CharacterEntry second) {
        if (targetGender == null || targetGender.isBlank() || "neutral".equalsIgnoreCase(targetGender)) {
            return 0.8;
        }
        String normalizedTarget = targetGender.toLowerCase(Locale.ROOT);
        return 0.5 * singleGenderScore(normalizedTarget, first)
                + 0.5 * singleGenderScore(normalizedTarget, second);
    }

    private double singleGenderScore(String target, CharacterEntry entry) {
        if ("male".equals(target)) {
            return ratioBasedScore(1.0, entry.maleRatio());
        }
        if ("female".equals(target)) {
            return ratioBasedScore(1.0, entry.femaleRatio());
        }
        if (entry.genderTags().contains("neutral")) {
            return 0.85;
        }
        return 0.7;
    }

    private double ratioBasedScore(double target, double actual) {
        return Math.max(0.2, 1.0 - Math.abs(target - actual));
    }

    private SurnameMapping resolveSurname(String rawSurname, List<CharacterEntry> pool) {
        if (rawSurname == null || rawSurname.isBlank()) {
            return new SurnameMapping("孙", "sun");
        }
        String trimmed = rawSurname.trim();
        if (containsChinese(trimmed)) {
            String hanzi = String.valueOf(trimmed.charAt(0));
            String pinyin = resolveSurnamePinyinFromPool(hanzi, pool);
            return new SurnameMapping(hanzi, pinyin);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        if (normalized.isBlank()) {
            return new SurnameMapping("孙", "sun");
        }
        SurnameMapping direct = SURNAME_MAPPINGS.get(normalized);
        if (direct != null) {
            return direct;
        }
        String token = mapper.mapEnglishToPinyinTokens(normalized).stream().findFirst().orElse("sun");
        String hanzi = pool.stream()
                .filter(c -> c.pinyin().equalsIgnoreCase(token))
                .min(Comparator.comparingInt(CharacterEntry::frequencyRank))
                .map(CharacterEntry::hanzi)
                .orElseGet(() -> fallbackSurnameByInitial(normalized.charAt(0)));
        String pinyin = resolveSurnamePinyinFromPool(hanzi, pool);
        return new SurnameMapping(hanzi, pinyin);
    }

    private String resolveSurnamePinyinFromPool(String surname, List<CharacterEntry> pool) {
        return pool.stream()
                .filter(c -> c.hanzi().equals(surname))
                .map(CharacterEntry::pinyin)
                .findFirst()
                .orElse("sun");
    }

    private boolean containsChinese(String text) {
        return text.codePoints().anyMatch(cp -> Character.UnicodeScript.of(cp) == Character.UnicodeScript.HAN);
    }

    private String fallbackSurnameByInitial(char initial) {
        return switch (initial) {
            case 's' -> "史";
            case 'l' -> "李";
            case 'w' -> "王";
            case 'z' -> "赵";
            case 'm' -> "马";
            case 'c' -> "陈";
            case 'h' -> "黄";
            case 'y' -> "杨";
            default -> "孙";
        };
    }

    private boolean isHardGenderMismatch(String targetGender, CharacterEntry first, CharacterEntry second) {
        if (targetGender == null || targetGender.isBlank() || "neutral".equalsIgnoreCase(targetGender)) {
            return false;
        }
        String normalized = targetGender.toLowerCase(Locale.ROOT);
        if ("male".equals(normalized)) {
            return first.femaleRatio() >= 0.70 || second.femaleRatio() >= 0.70;
        }
        if ("female".equals(normalized)) {
            return first.maleRatio() >= 0.70 || second.maleRatio() >= 0.70;
        }
        return false;
    }

    private record SurnameMapping(String hanzi, String pinyin) {
    }
}
