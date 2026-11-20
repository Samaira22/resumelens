package com.resumelens.engine;

import com.resumelens.model.AnalysisReport;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResumeAnalyzerTest {

    private static final String RESUME = """
            Jane Doe
            jane@example.com
            Education
            B.Tech in Computer Science
            Technical Skills
            Java, Python, Docker
            Projects
            Built an API in Java
            """;

    private final ResumeAnalyzer analyzer = new ResumeAnalyzer();

    @Test
    void combinesSkillEvidenceAndCompleteness() {
        AnalysisReport r = analyzer.analyze(RESUME, "Required: Java, Python, Docker.");

        assertEquals(Set.of("Java"), r.demonstratedSkills());
        assertEquals(Set.of("Docker", "Python"), r.listedOnlySkills());
        assertTrue(r.missingSections().contains("EXPERIENCE"));

        // S = 1.0, T = 1/3, C = 4/5  ->  0.7 + 0.0667 + 0.08 = 0.8467
        assertEquals(85, r.finalScore());
    }

    @Test
    void emptyResumeGivesZero() {
        AnalysisReport r = analyzer.analyze("", "Required: Java.");
        assertEquals(0, r.finalScore());
    }
}
