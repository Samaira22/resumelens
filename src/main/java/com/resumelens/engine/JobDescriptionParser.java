package com.resumelens.engine;

import com.resumelens.model.JobRequirements;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class JobDescriptionParser {

    private static final Pattern PREFERRED_MARKER = Pattern.compile(
            "\\b(preferred|nice to have|nice-to-have|good to have|bonus|plus|optional|desirable)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern REQUIRED_MARKER = Pattern.compile(
            "\\b(required|requirements?|must have|must-have|mandatory|essential)\\b",
            Pattern.CASE_INSENSITIVE);

    private final SkillExtractor extractor;

    public JobDescriptionParser(SkillExtractor extractor) {
        this.extractor = extractor;
    }

    public JobRequirements parse(String jobText) {
        Set<String> required = new TreeSet<>();
        Set<String> preferred = new TreeSet<>();

        if (jobText == null || jobText.isBlank()) {
            return new JobRequirements(required, preferred);
        }

        boolean inPreferredSection = false;

        // split into lines and sentences (a period followed by whitespace, or a semicolon)
        for (String segment : jobText.split("\\R|(?<=[.;])\\s+")) {
            if (segment.isBlank()) continue;

            if (PREFERRED_MARKER.matcher(segment).find()) {
                inPreferredSection = true;
            } else if (REQUIRED_MARKER.matcher(segment).find()) {
                inPreferredSection = false;
            }

            Set<String> skills = extractor.extract(segment);
            if (inPreferredSection) {
                preferred.addAll(skills);
            } else {
                required.addAll(skills);
            }
        }

        preferred.removeAll(required);   // if a skill is in both, required wins
        return new JobRequirements(required, preferred);
    }
}