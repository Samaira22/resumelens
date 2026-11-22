package com.resumelens.controller;

import com.jayway.jsonpath.JsonPath;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private String registerAndGetToken() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

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
    void analyzeRequiresLogin() throws Exception {
        mockMvc.perform(multipart("/api/analyze")
                        .file(sampleResume())
                        .param("jobDescription", "Required: Java."))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void analyzesAnUploadedDocx() throws Exception {
        String token = registerAndGetToken();
        mockMvc.perform(multipart("/api/analyze")
                        .file(sampleResume())
                        .param("jobDescription", "Required: Java, Python.")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").isNumber())
                .andExpect(jsonPath("$.required.matched", hasItem("Java")));
    }

    @Test
    void savesHistoryAndKeepsItPrivateToTheOwner() throws Exception {
        String alice = registerAndGetToken();
        String bob = registerAndGetToken();

        String body = mockMvc.perform(multipart("/api/analyze")
                        .file(sampleResume())
                        .param("jobDescription", "Required: Java, Python.")
                        .param("jobTitle", "Backend Intern")
                        .header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.id");

        // the owner sees it
        mockMvc.perform(get("/api/analyses").header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.jobTitle == 'Backend Intern')]").isNotEmpty());
        mockMvc.perform(get("/api/analyses/{id}", id.longValue()).header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk());

        // another user does not
        mockMvc.perform(get("/api/analyses").header("Authorization", "Bearer " + bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        mockMvc.perform(get("/api/analyses/{id}", id.longValue()).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/analyses/{id}", id.longValue()).header("Authorization", "Bearer " + bob))
                .andExpect(status().isNotFound());

        // the owner can delete it
        mockMvc.perform(delete("/api/analyses/{id}", id.longValue()).header("Authorization", "Bearer " + alice))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectsUnsupportedFileType() throws Exception {
        String token = registerAndGetToken();
        MockMultipartFile file = new MockMultipartFile("resume", "notes.txt",
                "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/analyze")
                        .file(file)
                        .param("jobDescription", "Required: Java.")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}