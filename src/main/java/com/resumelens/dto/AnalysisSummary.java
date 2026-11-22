package com.resumelens.dto;

import java.time.Instant;

public record AnalysisSummary(
        Long id,
        String resumeFileName,
        String jobTitle,
        int score,
        String label,
        Instant createdAt) {}
