package com.resumelens.dto;

import com.resumelens.model.AnalysisReport;
import com.resumelens.model.MatchResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record AnalysisResponse(
        Long id,
        int score,
        String label,
        String formula,
        double skillCoverage,
        double evidence,
        double completeness,
        SkillDetails required,
        SkillDetails preferred,
        Set<String> demonstratedSkills,
        Set<String> listedOnlySkills,
        List<String> missingSections,
        List<String> suggestions) {

    public record SkillDetails(Set<String> matched, Map<String, String> partial, Set<String> missing) {
        static SkillDetails from(MatchResult m) {
            return new SkillDetails(m.matched(), m.partial(), m.missing());
        }
    }

    public static AnalysisResponse from(AnalysisReport r) {
        return new AnalysisResponse(
                null,
                r.finalScore(),
                r.label(),
                "Score = 0.7*S + 0.2*T + 0.1*C",
                round(r.skills().skillCoverage()),
                round(r.evidence()),
                round(r.completeness()),
                SkillDetails.from(r.skills().required()),
                SkillDetails.from(r.skills().preferred()),
                r.demonstratedSkills(),
                r.listedOnlySkills(),
                r.missingSections(),
                r.suggestions());
    }

    public AnalysisResponse withId(Long newId) {
        return new AnalysisResponse(newId, score, label, formula, skillCoverage, evidence,
                completeness, required, preferred, demonstratedSkills, listedOnlySkills,
                missingSections, suggestions);
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}