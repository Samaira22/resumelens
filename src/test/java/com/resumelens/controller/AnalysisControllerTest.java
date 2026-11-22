package com.resumelens.controller;

import com.jayway.jsonpath.JsonPath;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private byte[] docx(String... lines) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            for (String line : lines) {
                doc.createParagraph().createRun().setText(line);
            }
            doc.write(out);
            return out.toByteArray();
        }
    }

    private MockMultipartFile sampleResume() throws IOException {
        return new MockMultipartFile("resume", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docx("Jane Doe", "jane@example.com", "Education", "B.Tech",
                        "Technical Skills", "Java, Python", "Projects", "Built an API in Java"));
    }

    @Test
    void analyzesAnUploadedDocx() throws Exception {
        mockMvc.perform(multipart("/api/analyze")
                        .file(sampleResume())
                        .param("jobDescription", "Required: Java, Python."))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.required.matched", hasItem("Java")));
    }

    @Test
    void savesAnalysisAndShowsItInHistory() throws Exception {
        String body = mockMvc.perform(multipart("/api/analyze")
                        .file(sampleResume())
                        .param("jobDescription", "Required: Java, Python.")
                        .param("jobTitle", "Backend Intern"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.id");

        mockMvc.perform(get("/api/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.jobTitle == 'Backend Intern')]").isNotEmpty());

        mockMvc.perform(get("/api/analyses/{id}", id.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.intValue()))
                .andExpect(jsonPath("$.required.matched", hasItem("Java")));

        mockMvc.perform(delete("/api/analyses/{id}", id.longValue()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/analyses/{id}", id.longValue()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsUnsupportedFileType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("resume", "notes.txt",
                "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/analyze")
                        .file(file)
                        .param("jobDescription", "Required: Java."))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingJobDescription() throws Exception {
        mockMvc.perform(multipart("/api/analyze").file(sampleResume()))
                .andExpect(status().isBadRequest());
    }
}