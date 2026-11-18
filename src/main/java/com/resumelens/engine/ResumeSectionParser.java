package com.resumelens.engine;

import com.resumelens.model.Section;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class ResumeSectionParser {

    private static final Map<Section, Pattern> HEADINGS = new LinkedHashMap<>();

    static {
        add(Section.EDUCATION, "education|academic background|academics");
        add(Section.SKILLS, "technical skills|skills|core skills|key skills|skills & tools|tools & technologies|technologies");
        add(Section.PROJECTS, "projects|academic projects|personal projects|key projects");
        add(Section.EXPERIENCE, "experience|work experience|professional experience|internships?|internship experience");
        add(Section.CERTIFICATIONS, "certifications?|certificates|courses");
        add(Section.OTHER, "achievements|awards|extracurricular activities|positions of responsibility|summary|objective|profile|publications|interests|hobbies");
    }

    private static void add(Section section, String alternatives) {
        HEADINGS.put(section, Pattern.compile("^(" + alternatives + ")$", Pattern.CASE_INSENSITIVE));
    }

    public Map<Section, String> parse(String text) {
        Map<Section, StringBuilder> builders = new EnumMap<>(Section.class);
        if (text == null || text.isBlank()) return new EnumMap<>(Section.class);

        Section current = Section.HEADER;

        for (String line : text.split("\\R")) {
            Section heading = detectHeading(line);
            if (heading != null) {
                current = heading;
                continue;               // the heading line itself is not content
            }
            builders.computeIfAbsent(current, k -> new StringBuilder())
                    .append(line).append('\n');
        }

        Map<Section, String> result = new EnumMap<>(Section.class);
        builders.forEach((k, v) -> result.put(k, v.toString().trim()));
        return result;
    }

    // everything except the header (name, phone, email, links)
    public String contentText(Map<Section, String> sections) {
        StringBuilder sb = new StringBuilder();
        sections.forEach((section, body) -> {
            if (section != Section.HEADER) sb.append(body).append('\n');
        });
        return sb.toString();
    }

    private Section detectHeading(String line) {
        String cleaned = line.trim().replaceAll("[:\\s]+$", "");
        if (cleaned.isEmpty() || cleaned.length() > 40) return null;

        for (Map.Entry<Section, Pattern> e : HEADINGS.entrySet()) {
            if (e.getValue().matcher(cleaned).matches()) return e.getKey();
        }
        return null;
    }
}