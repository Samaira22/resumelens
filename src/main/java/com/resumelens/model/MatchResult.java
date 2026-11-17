package com.resumelens.model;

import java.util.Map;
import java.util.Set;

public record MatchResult(
        Set<String> matched,            // exact matches
        Map<String, String> partial,    // required skill -> related skill found in resume
        Set<String> missing) {

    public int totalRequired() {
        return matched.size() + partial.size() + missing.size();
    }

    // exact = 1.0, partial = 0.5, missing = 0
    public double coverage() {
        int total = totalRequired();
        if (total == 0) return 0.0;
        return (matched.size() + 0.5 * partial.size()) / total;
    }
}
