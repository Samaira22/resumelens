package com.resumelens.model;

import java.util.List;
import java.util.Set;

public record AnalysisReport(
        ScoreReport skills,
        double evidence,                  // T: 0..1
        double completeness,              // C: 0..1
        Set<String> demonstratedSkills,   // matched AND backed by projects/experience
        Set<String> listedOnlySkills,     // matched but only listed
        List<String> missingSections,
        List<String> suggestions) {

    public int finalScore() {
        double score = 0.7 * skills.skillCoverage() + 0.2 * evidence + 0.1 * completeness;
        return (int) Math.round(score * 100);
    }

    public String label() {
        int s = finalScore();
        if (s >= 80) return "Strong match";
        if (s >= 60) return "Good match";
        if (s >= 40) return "Partial match";
        return "Weak match";
    }
}