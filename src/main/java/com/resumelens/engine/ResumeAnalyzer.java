package com.resumelens.engine;

import com.resumelens.model.AnalysisReport;
import com.resumelens.model.JobRequirements;
import com.resumelens.model.ScoreReport;
import com.resumelens.model.Section;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class ResumeAnalyzer {

    private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.]+");

    private final SkillExtractor extractor;
    private final JobDescriptionParser jobParser;
    private final ResumeSectionParser sectionParser;
    private final ScoreCalculator calculator;
    private final SuggestionGenerator suggestionGenerator;

    public ResumeAnalyzer() {
        this.extractor = new SkillExtractor();
        this.jobParser = new JobDescriptionParser(extractor);
        this.sectionParser = new ResumeSectionParser();
        this.calculator = new ScoreCalculator(new SkillMatcher());
        this.suggestionGenerator = new SuggestionGenerator();
    }

    public AnalysisReport analyze(String resumeText, String jobText) {
        Map<Section, String> sections = sectionParser.parse(resumeText);

        // S: skill coverage (header excluded so contact links don't count)
        Set<String> resumeSkills = extractor.extract(sectionParser.contentText(sections));
        JobRequirements job = jobParser.parse(jobText);
        ScoreReport skillReport = calculator.calculate(resumeSkills, job);

        // T: which matched skills are backed by projects or experience?
        Set<String> exactMatched = new TreeSet<>(skillReport.required().matched());
        exactMatched.addAll(skillReport.preferred().matched());

        String projectsText = sections.getOrDefault(Section.PROJECTS, "");
        String evidenceText = projectsText + "\n" + sections.getOrDefault(Section.EXPERIENCE, "");
        Set<String> inEvidence = extractor.extract(evidenceText);

        Set<String> demonstrated = new TreeSet<>(exactMatched);
        demonstrated.retainAll(inEvidence);
        Set<String> listedOnly = new TreeSet<>(exactMatched);
        listedOnly.removeAll(inEvidence);

        int totalJobSkills = job.required().size() + job.preferred().size();
        double evidence = totalJobSkills == 0 ? 0.0 : (double) demonstrated.size() / totalJobSkills;

        // C: completeness checklist (5 items, 20% each)
        List<String> missingSections = new ArrayList<>();
        int present = 0;

        if (resumeText != null && EMAIL.matcher(resumeText).find()) present++;
        else missingSections.add("CONTACT EMAIL");

        for (Section s : List.of(Section.EDUCATION, Section.SKILLS, Section.PROJECTS, Section.EXPERIENCE)) {
            String body = sections.get(s);
            if (body != null && !body.isBlank()) present++;
            else missingSections.add(s.name());
        }
        double completeness = present / 5.0;

        List<String> suggestions = suggestionGenerator.generate(
                skillReport, listedOnly, missingSections, projectsText);

        return new AnalysisReport(skillReport, evidence, completeness,
                demonstrated, listedOnly, missingSections, suggestions);
    }
}