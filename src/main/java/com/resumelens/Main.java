package com.resumelens;

import com.resumelens.engine.JobDescriptionParser;
import com.resumelens.engine.ResumeSectionParser;
import com.resumelens.engine.ResumeTextExtractor;
import com.resumelens.engine.ScoreCalculator;
import com.resumelens.engine.SkillExtractor;
import com.resumelens.engine.SkillMatcher;
import com.resumelens.model.JobRequirements;
import com.resumelens.model.ScoreReport;
import com.resumelens.model.Section;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Exception {
        SkillExtractor extractor = new SkillExtractor();
        SkillMatcher matcher = new SkillMatcher();
        JobDescriptionParser parser = new JobDescriptionParser(extractor);
        ScoreCalculator calculator = new ScoreCalculator(matcher);
        ResumeTextExtractor fileReader = new ResumeTextExtractor();
        ResumeSectionParser sectionParser = new ResumeSectionParser();

        // 1. Read the resume file
        Path path = Path.of("C:/ResumeLens/resumelens/Samples/resume.pdf");
        String resumeText = fileReader.extract(path);

        // 2. Split into sections
        Map<Section, String> sections = sectionParser.parse(resumeText);
        System.out.println("Sections found: " + sections.keySet());

        // 3. Extract skills from everything except the header (name, phone, links)
        Set<String> resumeSkills = extractor.extract(sectionParser.contentText(sections));

        // 4. Parse the job description
        String job = """
                Required: Python, Machine Learning, Pandas, NumPy.
                Preferred: TensorFlow, SQL, Git, Docker.
                """;
        JobRequirements requirements = parser.parse(job);

        // 5. Score
        ScoreReport report = calculator.calculate(resumeSkills, requirements);

        System.out.println();
        System.out.println("Skills found in resume: " + resumeSkills);
        System.out.println("Required matched:  " + report.required().matched());
        System.out.println("Required partial:  " + report.required().partial());
        System.out.println("Required missing:  " + report.required().missing());
    }
}