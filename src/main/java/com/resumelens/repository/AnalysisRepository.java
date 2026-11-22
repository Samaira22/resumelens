package com.resumelens.repository;

import com.resumelens.dto.AnalysisSummary;
import com.resumelens.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    @Query("""
            select new com.resumelens.dto.AnalysisSummary(
                a.id, r.fileName, j.title, a.score, a.label, a.createdAt)
            from Analysis a
            join a.resume r
            join a.jobDescription j
            where a.owner.id = :userId
            order by a.createdAt desc
            """)
    List<AnalysisSummary> findSummariesByOwner(@Param("userId") Long userId);

    Optional<Analysis> findByIdAndOwnerId(Long id, Long ownerId);
}