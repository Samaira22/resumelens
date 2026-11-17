package com.resumelens.model;

public record ScoreReport(MatchResult required, MatchResult preferred, double skillCoverage) {

    public int score() {
        return (int) Math.round(skillCoverage * 100);
    }

    public String label() {
        int s = score();
        if (s >= 80) return "Strong match";
        if (s >= 60) return "Good match";
        if (s >= 40) return "Partial match";
        return "Weak match";
    }
}