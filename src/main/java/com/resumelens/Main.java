package com.resumelens;

import com.resumelens.engine.JobDescriptionParser;
import com.resumelens.engine.ScoreCalculator;
import com.resumelens.engine.SkillExtractor;
import com.resumelens.engine.SkillMatcher;
import com.resumelens.model.JobRequirements;
import com.resumelens.model.ScoreReport;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        SkillExtractor extractor = new SkillExtractor();
        SkillMatcher matcher = new SkillMatcher();
        JobDescriptionParser parser = new JobDescriptionParser(extractor);
        ScoreCalculator calculator = new ScoreCalculator(matcher);

        String resume = "Built a REST API using Java and ReactJS with PostgreSQL. "
                + "Worked on ML projects in Python.";

        String job = """
                Required: Java, SQL, REST API.
                Preferred: Docker, AWS, Python.
                """;

        Set<String> resumeSkills = extractor.extract(resume);
        JobRequirements requirements = parser.parse(job);
        ScoreReport report = calculator.calculate(resumeSkills, requirements);

        System.out.println("Required matched: " + report.required().matched());
        System.out.println("Required partial: " + report.required().partial());
        System.out.println("Required missing: " + report.required().missing());
        System.out.println("Preferred matched: " + report.preferred().matched());
        System.out.println("Preferred missing: " + report.preferred().missing());
        System.out.println("Score: " + report.score() + "/100 (" + report.label() + ")");
    }
}