package edu.cs6103.chinesename.model;

import java.util.Set;

public record CharacterEntry(
        String hanzi,
        String pinyin,
        int tone,
        String meaning,
        String sourceNote,
        Set<String> styleTags,
        Set<String> genderTags,
        double maleRatio,
        double femaleRatio,
        int frequencyRank
) {
}
