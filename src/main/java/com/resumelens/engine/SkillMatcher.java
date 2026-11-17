package com.resumelens.engine;

import com.resumelens.model.MatchResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SkillMatcher {

    // required skill -> related skills that earn partial credit
    private final Map<String, Set<String>> related = new HashMap<>();

    public SkillMatcher() {
        loadRelated("/related.txt");
    }

    private void loadRelated(String resourcePath) {
        InputStream in = getClass().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalStateException("Could not find " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(":", 2);
                if (parts.length < 2) continue;

                Set<String> alternatives = new LinkedHashSet<>();
                for (String alt : parts[1].split(",")) {
                    alternatives.add(alt.trim());
                }
                related.put(parts[0].trim(), alternatives);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + resourcePath, e);
        }
    }

    public MatchResult match(Set<String> resumeSkills, Set<String> requiredSkills) {
        Set<String> matched = new TreeSet<>();
        Map<String, String> partial = new TreeMap<>();
        Set<String> missing = new TreeSet<>();

        for (String required : new TreeSet<>(requiredSkills)) {
            if (resumeSkills.contains(required)) {
                matched.add(required);
                continue;
            }
            String relatedFound = findRelated(required, resumeSkills);
            if (relatedFound != null) {
                partial.put(required, relatedFound);
            } else {
                missing.add(required);
            }
        }
        return new MatchResult(matched, partial, missing);
    }

    private String findRelated(String required, Set<String> resumeSkills) {
        for (String alt : related.getOrDefault(required, Collections.emptySet())) {
            if (resumeSkills.contains(alt)) return alt;
        }
        return null;
    }
}
