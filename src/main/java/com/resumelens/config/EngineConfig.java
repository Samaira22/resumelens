package com.resumelens.config;

import com.resumelens.engine.ResumeAnalyzer;
import com.resumelens.engine.ResumeTextExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EngineConfig {

    @Bean
    public ResumeAnalyzer resumeAnalyzer() {
        return new ResumeAnalyzer();
    }

    @Bean
    public ResumeTextExtractor resumeTextExtractor() {
        return new ResumeTextExtractor();
    }
}
