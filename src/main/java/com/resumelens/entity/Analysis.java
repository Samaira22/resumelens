package com.resumelens.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "analyses")
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false,
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY, optional = false,
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name = "job_description_id", nullable = false)
    private JobDescription jobDescription;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private String label;

    // the full result, stored as JSON text
    @Lob
    @Column(name = "result_json", nullable = false)
    private String resultJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Analysis() {}

    public Analysis(Resume resume, JobDescription jobDescription,
                    int score, String label, String resultJson) {
        this.resume = resume;
        this.jobDescription = jobDescription;
        this.score = score;
        this.label = label;
        this.resultJson = resultJson;
    }

    public Long getId() { return id; }
    public Resume getResume() { return resume; }
    public JobDescription getJobDescription() { return jobDescription; }
    public int getScore() { return score; }
    public String getLabel() { return label; }
    public String getResultJson() { return resultJson; }
    public Instant getCreatedAt() { return createdAt; }
}
