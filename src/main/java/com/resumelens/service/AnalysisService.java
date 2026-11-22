package com.resumelens.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumelens.dto.AnalysisResponse;
import com.resumelens.dto.AnalysisSummary;
import com.resumelens.engine.ResumeAnalyzer;
import com.resumelens.engine.ResumeTextExtractor;
import com.resumelens.entity.Analysis;
import com.resumelens.entity.JobDescription;
import com.resumelens.entity.Resume;
import com.resumelens.exception.ApiException;
import com.resumelens.repository.AnalysisRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class AnalysisService {

    private final ResumeAnalyzer analyzer;
    private final ResumeTextExtractor extractor;
    private final AnalysisRepository repository;
    private final ObjectMapper objectMapper;

    public AnalysisService(ResumeAnalyzer analyzer, ResumeTextExtractor extractor,
                           AnalysisRepository repository, ObjectMapper objectMapper) {
        this.analyzer = analyzer;
        this.extractor = extractor;
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public AnalysisResponse analyze(MultipartFile file, String jobDescription, String jobTitle) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The resume file is empty.");
        }
        if (jobDescription == null || jobDescription.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The job description is empty.");
        }

        String extension = extensionOf(file.getOriginalFilename());
        if (!extension.equals(".pdf") && !extension.equals(".docx")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PDF and DOCX files are supported.");
        }

        String text = readText(file, extension);
        AnalysisResponse result = AnalysisResponse.from(analyzer.analyze(text, jobDescription));

        Resume resume = new Resume(safeFileName(file.getOriginalFilename()), text);
        JobDescription job = new JobDescription(cleanTitle(jobTitle), jobDescription);
        Analysis saved = repository.save(
                new Analysis(resume, job, result.score(), result.label(), toJson(result)));

        return result.withId(saved.getId());
    }

    public List<AnalysisSummary> list() {
        return repository.findSummaries();
    }

    public AnalysisResponse get(Long id) {
        Analysis analysis = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Analysis not found: " + id));
        try {
            return objectMapper.readValue(analysis.getResultJson(), AnalysisResponse.class)
                    .withId(analysis.getId());
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Stored analysis could not be read.");
        }
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Analysis not found: " + id);
        }
        repository.deleteById(id);
    }

    // ---- helpers ----

    private String readText(MultipartFile file, String extension) {
        Path temp = null;
        try {
            // our own temp name: the user's filename is never used on disk
            temp = Files.createTempFile("resumelens-", extension);
            Files.copy(file.getInputStream(), temp, StandardCopyOption.REPLACE_EXISTING);

            String text = extractor.extract(temp);
            if (text.isBlank()) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "No text could be read from the file. If it is a scanned PDF, "
                                + "upload a text-based PDF or a DOCX instead.");
            }
            return text;
        } catch (ApiException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Could not read the resume. Is it a valid PDF or DOCX?");
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            }
        }
    }

    private String toJson(AnalysisResponse result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store the analysis.");
        }
    }

    private String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot).toLowerCase();
    }

    private String safeFileName(String original) {
        if (original == null || original.isBlank()) return "resume";
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1);
        return name.length() > 200 ? name.substring(0, 200) : name;
    }

    private String cleanTitle(String title) {
        if (title == null || title.isBlank()) return "Untitled job";
        String t = title.trim();
        return t.length() > 200 ? t.substring(0, 200) : t;
    }
}