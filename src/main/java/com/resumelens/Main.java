package com.resumelens;

import com.resumelens.engine.JobDescriptionParser;
import com.resumelens.engine.ResumeTextExtractor;
import com.resumelens.engine.ScoreCalculator;
import com.resumelens.engine.SkillExtractor;
import com.resumelens.engine.SkillMatcher;
import com.resumelens.model.JobRequirements;
import com.resumelens.model.ScoreReport;

import java.nio.file.Path;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        SkillExtractor extractor = new SkillExtractor();
        SkillMatcher matcher = new SkillMatcher();
        JobDescriptionParser parser = new JobDescriptionParser(extractor);
        ScoreCalculator calculator = new ScoreCalculator(matcher);
        ResumeTextExtractor fileReader = new ResumeTextExtractor();

        String resumeText = fileReader.extract(Path.of("C:/ResumeLens/resumelens/Samples/resume.pdf"));
        System.out.println("--- Extracted text (first 300 chars) ---");
        System.out.println(resumeText.substring(0, Math.min(300, resumeText.length())));
        System.out.println();

        String job = """
        Required: Python, Machine Learning, Pandas, NumPy.
        Preferred: TensorFlow, SQL, Git, Docker.
        """;

        Set<String> resumeSkills = extractor.extract(resumeText);
        JobRequirements requirements = parser.parse(job);
        ScoreReport report = calculator.calculate(resumeSkills, requirements);

        System.out.println("Skills found in resume: " + resumeSkills);
        System.out.println("Required matched: " + report.required().matched());
        System.out.println("Required partial: " + report.required().partial());
        System.out.println("Required missing: " + report.required().missing());
        System.out.println("Preferred matched: " + report.preferred().matched());
        System.out.println("Preferred missing: " + report.preferred().missing());
        System.out.println("Score: " + report.score() + "/100 (" + report.label() + ")");
    }
}