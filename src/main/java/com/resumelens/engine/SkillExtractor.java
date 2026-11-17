package com.resumelens.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class SkillExtractor {

    // synonym (lowercase) -> canonical skill name
    private final Map<String, String> synonymToCanonical = new HashMap<>();

    // synonym -> precompiled regex
    private final Map<String, Pattern> patterns = new HashMap<>();

    public SkillExtractor() {
        loadSkills("/skills.txt");
    }

    private void loadSkills(String resourcePath) {
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

                String canonical = parts[0].trim();
                for (String synonym : parts[1].split(",")) {
                    add(canonical, synonym.trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + resourcePath, e);
        }
    }

    private void add(String canonical, String synonym) {
        String key = synonym.toLowerCase();
        synonymToCanonical.put(key, canonical);

        // whole-term match: "java" must NOT match inside "javascript"
        String regex = "(?<![a-z0-9+#])" + Pattern.quote(key) + "(?![a-z0-9+#])";
        patterns.put(key, Pattern.compile(regex));
    }

    public Set<String> extract(String text) {
        Set<String> found = new TreeSet<>();
        if (text == null || text.isBlank()) return found;

        String lower = text.toLowerCase();
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
            if (entry.getValue().matcher(lower).find()) {
                found.add(synonymToCanonical.get(entry.getKey()));
            }
        }
        return found;
    }
}
