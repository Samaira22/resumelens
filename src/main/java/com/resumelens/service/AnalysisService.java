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
import com.resumelens.repository.UserRepository;
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
    private final UserRepository users;
    private final ObjectMapper objectMapper;

    public AnalysisService(ResumeAnalyzer analyzer, ResumeTextExtractor extractor,
                           AnalysisRepository repository, UserRepository users,
                           ObjectMapper objectMapper) {
        this.analyzer = analyzer;
        this.extractor = extractor;
        this.repository = repository;
        this.users = users;
        this.objectMapper = objectMapper;
    }

    public AnalysisResponse analyze(Long userId, MultipartFile file, String jobDescription, String jobTitle) {
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
        Analysis saved = repository.save(new Analysis(
                users.getReferenceById(userId), resume, job,
                result.score(), result.label(), toJson(result)));

        return result.withId(saved.getId());
    }

    public List<AnalysisSummary> list(Long userId) {
        return repository.findSummariesByOwner(userId);
    }

    public AnalysisResponse get(Long userId, Long id) {
        Analysis analysis = findOwned(userId, id);
        try {
            return objectMapper.readValue(analysis.getResultJson(), AnalysisResponse.class)
                    .withId(analysis.getId());
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Stored analysis could not be read.");
        }
    }

    public void delete(Long userId, Long id) {
        repository.delete(findOwned(userId, id));
    }

    // A missing analysis and someone else's analysis both give 404,
    // so users cannot discover which ids exist.
    private Analysis findOwned(Long userId, Long id) {
        return repository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Analysis not found: " + id));
    }

    private String readText(MultipartFile file, String extension) {
        Path temp = null;
        try {
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