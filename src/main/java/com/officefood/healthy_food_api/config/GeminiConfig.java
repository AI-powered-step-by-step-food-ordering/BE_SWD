package com.officefood.healthy_food_api.config;

import com.google.genai.Client;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class GeminiConfig {

    @Value("${app.gemini.api-key}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-2.0-flash-exp}")
    private String model;

    @Value("${app.gemini.temperature:0.7}")
    private Double temperature;

    @Value("${app.gemini.max-tokens:1000}")
    private Integer maxTokens;

    @Value("${app.gemini.timeout:30000}")
    private Integer timeout;

    @Bean
    public Client geminiClient() {
        // Pass API key directly to Client constructor
        // The API key can be from environment variable GEMINI_API_KEY or from application.yml
        return new Client.Builder()
            .apiKey(apiKey)
            .build();
    }
}

