package com.resumelens.engine;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResumeTextExtractor {

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
                .replace('\u00A0', ' ')
                .replace("\uFB01", "fi")    // ﬁ
                .replace("\uFB02", "fl")    // ﬂ
                .replace("\uFB00", "ff")    // ﬀ
                .replace("\uFB03", "ffi")   // ﬃ
                .replace("\uFB04", "ffl")   // ﬄ
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
