package com.resumelens.controller;

import com.resumelens.dto.AnalysisResponse;
import com.resumelens.dto.AnalysisSummary;
import com.resumelens.service.AnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService service;

    public AnalysisController(AnalysisService service) {
        this.service = service;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AnalysisResponse analyze(@RequestParam("resume") MultipartFile resume,
                                    @RequestParam("jobDescription") String jobDescription,
                                    @RequestParam(value = "jobTitle", required = false) String jobTitle) {
        return service.analyze(resume, jobDescription, jobTitle);
    }

    @GetMapping("/analyses")
    public List<AnalysisSummary> list() {
        return service.list();
    }

    @GetMapping("/analyses/{id}")
    public AnalysisResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @DeleteMapping("/analyses/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}