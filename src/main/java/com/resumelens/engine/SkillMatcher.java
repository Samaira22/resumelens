package com.resumelens.engine;

import com.resumelens.model.MatchResult;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static class ResumeTextExtractor {

        public String extract(Path file) throws IOException {
            if (file == null || !Files.exists(file)) {
                throw new IOException("File not found: " + file);
            }

            String name = file.getFileName().toString().toLowerCase();
            String text;

            if (name.endsWith(".pdf")) {
                text = extractPdf(file);
            } else if (name.endsWith(".docx")) {
                text = extractDocx(file);
            } else {
                throw new IllegalArgumentException("Unsupported file type (use .pdf or .docx): " + name);
            }
            return normalize(text);
        }

        private String extractPdf(Path file) throws IOException {
            try (PDDocument document = Loader.loadPDF(file.toFile())) {
                return new PDFTextStripper().getText(document);
            }
        }

        private String extractDocx(Path file) throws IOException {
            try (InputStream in = Files.newInputStream(file);
                 XWPFDocument document = new XWPFDocument(in);
                 XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                return extractor.getText();
            }
        }

        // clean up whitespace so later steps see consistent text
        private String normalize(String text) {
            if (text == null) return "";
            return text.replace("\r\n", "\n")
                    .replace('\u00A0', ' ')          // non-breaking spaces
                    .replaceAll("[ \\t]+", " ")      // collapse repeated spaces/tabs
                    .replaceAll("\\n{3,}", "\n\n")   // collapse blank lines
                    .trim();
        }
    }
}
