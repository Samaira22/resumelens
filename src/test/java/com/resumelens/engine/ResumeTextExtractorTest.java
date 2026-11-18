package com.resumelens.engine;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ResumeTextExtractorTest {

    private final ResumeTextExtractor extractor = new ResumeTextExtractor();

    @Test
    void readsTextFromDocx(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("resume.docx");
        try (XWPFDocument doc = new XWPFDocument();
             OutputStream out = Files.newOutputStream(file)) {
            XWPFParagraph p = doc.createParagraph();
            p.createRun().setText("Skills: Java, SQL, Docker");
            doc.write(out);
        }
        String text = extractor.extract(file);
        assertTrue(text.contains("Java"));
        assertTrue(text.contains("Docker"));
    }

    @Test
    void rejectsUnsupportedFileType(@TempDir Path dir) throws IOException {
        Path file = Files.writeString(dir.resolve("resume.txt"), "hello");
        assertThrows(IllegalArgumentException.class, () -> extractor.extract(file));
    }

    @Test
    void missingFileThrows() {
        assertThrows(IOException.class, () -> extractor.extract(Path.of("no-such-file.pdf")));
    }
}
