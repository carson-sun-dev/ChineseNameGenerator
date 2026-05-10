package dev.carson.chinesename.service;

public final class ScoringModel {
    private ScoringModel() {
    }

    public static double score(
            double phoneticScore,
            double styleScore,
            double rarityScore,
            double toneHarmonyScore,
            double genderScore,
            double penalties
    ) {
        return 0.50 * phoneticScore
                + 0.25 * styleScore
                + 0.10 * rarityScore
                + 0.05 * toneHarmonyScore
                + 0.10 * genderScore
                - penalties;
    }
}
