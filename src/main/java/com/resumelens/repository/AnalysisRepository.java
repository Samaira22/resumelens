package com.resumelens.repository;

import com.resumelens.dto.AnalysisSummary;
import com.resumelens.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    @Query("""
            select new com.resumelens.dto.AnalysisSummary(
                a.id, r.fileName, j.title, a.score, a.label, a.createdAt)
            from Analysis a
            join a.resume r
            join a.jobDescription j
            order by a.createdAt desc
            """)
    List<AnalysisSummary> findSummaries();
}
