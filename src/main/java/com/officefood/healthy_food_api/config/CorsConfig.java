package com.officefood.healthy_food_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:3000", // NextJS development
                        "http://localhost:3001", // Alternative port
                        "http://localhost:5173", // Vite development
                        "http://localhost:8080", // Backend for testing
                        "http://127.0.0.1:3000", // Alternative localhost
                        "http://127.0.0.1:3001", // Alternative localhost
                        "http://127.0.0.1:5173", // Alternative localhost
                        "https://*.vercel.app"   // Vercel deployments
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
