package com.resumelens.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SkillExtractorTest {

    private SkillExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new SkillExtractor();
    }

    @Test
    void javaShouldNotMatchInsideJavaScript() {
        Set<String> skills = extractor.extract("Experienced with JavaScript");
        assertTrue(skills.contains("JavaScript"));
        assertFalse(skills.contains("Java"));
    }

    @Test
    void synonymsMapToCanonicalName() {
        assertTrue(extractor.extract("Built UIs with ReactJS").contains("React"));
    }

    @Test
    void abbreviationMapsToFullName() {
        assertTrue(extractor.extract("Worked on ML models").contains("Machine Learning"));
    }

    @Test
    void emptyTextReturnsEmptySet() {
        assertTrue(extractor.extract("").isEmpty());
        assertTrue(extractor.extract(null).isEmpty());
    }

    @Test
    void matchingIsCaseInsensitive() {
        Set<String> skills = extractor.extract("PYTHON and Docker");
        assertTrue(skills.contains("Python"));
        assertTrue(skills.contains("Docker"));
    }
}
