package edu.cs6103.chinesename.model;

public record NameCandidate(
        String surname,
        String givenName,
        String pinyin,
        double score,
        String explanation
) {
    public String fullName() {
        return surname + givenName;
    }
}
