package com.resumelens;

import com.resumelens.engine.SkillExtractor;
import com.resumelens.engine.SkillMatcher;
import com.resumelens.model.MatchResult;

import java.util.Set;

public class Main {
    public static void main(String[] args) {
        SkillExtractor extractor = new SkillExtractor();
        SkillMatcher matcher = new SkillMatcher();

        String resume = "Built a REST API using Java and ReactJS with PostgreSQL. Worked on ML projects in Python.";
        String job = "Looking for a Java developer with SQL, Docker and REST API experience.";

        Set<String> resumeSkills = extractor.extract(resume);
        Set<String> jobSkills = extractor.extract(job);

        MatchResult result = matcher.match(resumeSkills, jobSkills);

        System.out.println("Matched: " + result.matched());
        System.out.println("Partial: " + result.partial());
        System.out.println("Missing: " + result.missing());
        System.out.printf("Coverage: %.0f%%%n", result.coverage() * 100);
    }
}