package com.resumelens;

import com.resumelens.engine.ResumeAnalyzer;
import com.resumelens.engine.ResumeTextExtractor;
import com.resumelens.model.AnalysisReport;

import java.nio.file.Path;

public class ConsoleDemo {
    public static void main(String[] args) throws Exception {
        String resumeText = new ResumeTextExtractor()
                .extract(Path.of("C:/ResumeLens/resumelens/Samples/resume.pdf"));

        String job = """
        Required: Python, Computer Vision, SQL.
        Preferred: YOLO, Docker, Unit Testing.
        """;

        AnalysisReport r = new ResumeAnalyzer().analyze(resumeText, job);

        System.out.println("Required matched:  " + r.skills().required().matched());
        System.out.println("Required partial:  " + r.skills().required().partial());
        System.out.println("Required missing:  " + r.skills().required().missing());
        System.out.println("Preferred matched: " + r.skills().preferred().matched());
        System.out.println("Preferred partial: " + r.skills().preferred().partial());
        System.out.println("Preferred missing: " + r.skills().preferred().missing());
        System.out.println();
        System.out.println("Demonstrated in projects/experience: " + r.demonstratedSkills());
        System.out.println("Listed only (no project evidence):   " + r.listedOnlySkills());
        System.out.println("Missing resume sections:             " + r.missingSections());
        System.out.println();
        System.out.printf("S (skills)       = %.2f%n", r.skills().skillCoverage());
        System.out.printf("T (evidence)     = %.2f%n", r.evidence());
        System.out.printf("C (completeness) = %.2f%n", r.completeness());
        System.out.println("FINAL SCORE: " + r.finalScore() + "/100 (" + r.label() + ")");
        System.out.println();
        System.out.println("Suggestions:");
        int i = 1;
        for (String s : r.suggestions()) {
            System.out.println(i++ + ". " + s);
        }
    }
}