package com.resumelens.engine;

import com.resumelens.model.Section;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResumeSectionParserTest {

    private final ResumeSectionParser parser = new ResumeSectionParser();

    private static final String SAMPLE = """
            Jane Doe
            jane@example.com | github.com/jane
            Education
            B.Tech in Computer Science
            Technical Skills:
            Java, Python
            Projects
            Built a REST API with Spring Boot
            """;

    @Test
    void splitsIntoSections() {
        Map<Section, String> s = parser.parse(SAMPLE);
        assertTrue(s.get(Section.HEADER).contains("Jane Doe"));
        assertTrue(s.get(Section.EDUCATION).contains("B.Tech"));
        assertTrue(s.get(Section.SKILLS).contains("Java"));
        assertTrue(s.get(Section.PROJECTS).contains("Spring Boot"));
    }

    @Test
    void headerIsExcludedFromContent() {
        Map<Section, String> s = parser.parse(SAMPLE);
        String content = parser.contentText(s);
        assertFalse(content.contains("github.com"));
        assertTrue(content.contains("Java"));
    }

    @Test
    void emptyTextGivesEmptyMap() {
        assertTrue(parser.parse("").isEmpty());
        assertTrue(parser.parse(null).isEmpty());
    }

    @Test
    void longLinesAreNotTreatedAsHeadings() {
        Map<Section, String> s = parser.parse("Name\nSkills are important for building real software projects");
        assertEquals(1, s.size());
        assertTrue(s.containsKey(Section.HEADER));
    }
}
