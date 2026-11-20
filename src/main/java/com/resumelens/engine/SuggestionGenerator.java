package com.resumelens.engine;

import com.resumelens.model.ScoreReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SuggestionGenerator {

    private static final int MAX_SUGGESTIONS = 8;

    // a percentage, or a number followed by a unit such as "5,000 records"
    private static final Pattern MEASURABLE = Pattern.compile(
            "\\d+(\\.\\d+)?\\s*%"
                    + "|\\b\\d[\\d,]*\\+?\\s*(users|records|rows|samples|images|requests|datasets|models|features|ms|seconds)\\b",
            Pattern.CASE_INSENSITIVE);

    public List<String> generate(ScoreReport skills,
                                 Set<String> listedOnly,
                                 List<String> missingSections,
                                 String projectsText) {
        List<String> out = new ArrayList<>();

        // 1. Required skills the resume does not show at all (highest priority)
        for (String skill : skills.required().missing()) {
            out.add("Required skill missing: " + skill
                    + ". Add a project, course or certification that shows it, "
                    + "then list it in your Skills section.");
        }

        // 2. Related skills that could be named explicitly
        addPartialSuggestions(out, skills.required().partial());
        addPartialSuggestions(out, skills.preferred().partial());

        // 3. Skills that are only listed, not shown in a project
        if (!listedOnly.isEmpty()) {
            out.add("These skills appear only in your skills list: " + String.join(", ", listedOnly)
                    + ". Mention them in project bullets, for example "
                    + "'Cleaned a 10,000-row dataset using Pandas'.");
        }

        // 4. Missing resume sections
        for (String section : missingSections) {
            out.add(sectionAdvice(section));
        }

        // 5. Projects without measurable results
        if (projectsText != null && !projectsText.isBlank()
                && !MEASURABLE.matcher(projectsText).find()) {
            out.add("Add measurable results to your projects (for example "
                    + "'improved accuracy by 12%' or 'processed 5,000 records').");
        }

        // 6. Preferred skills that are missing (lowest priority)
        if (!skills.preferred().missing().isEmpty()) {
            out.add("Preferred skills you could add: " + String.join(", ", skills.preferred().missing())
                    + ". Pick one or two and build a small project with them.");
        }

        return out.size() > MAX_SUGGESTIONS ? new ArrayList<>(out.subList(0, MAX_SUGGESTIONS)) : out;
    }

    private void addPartialSuggestions(List<String> out, Map<String, String> partial) {
        for (Map.Entry<String, String> e : partial.entrySet()) {
            out.add("The job asks for " + e.getKey() + " and your resume shows " + e.getValue()
                    + ". If you have used " + e.getKey() + ", name it explicitly.");
        }
    }

    private String sectionAdvice(String section) {
        return switch (section) {
            case "EXPERIENCE" -> "Add an Experience section. Internships, freelance work, "
                    + "open-source contributions and hackathons all count.";
            case "CONTACT EMAIL" -> "Add a professional email address to your contact details.";
            default -> "Add a " + section.charAt(0) + section.substring(1).toLowerCase() + " section.";
        };
    }
}
