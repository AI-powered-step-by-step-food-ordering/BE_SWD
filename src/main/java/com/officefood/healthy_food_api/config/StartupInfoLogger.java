package com.officefood.healthy_food_api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupInfoLogger implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupInfoLogger.class);

    @Value("${server.port:8080}")
    private int port;

    @Value("${app.swagger.enabled:true}")
    private boolean swaggerEnabled;

    @Override
    public void run(String... args) {
        String base = "http://localhost:" + port;
        log.info("Application is running at: {}", base);
        if (swaggerEnabled) {
            log.info("Swagger UI: {}/swagger-ui/index.html", base);
            log.info("OpenAPI JSON: {}/v3/api-docs", base);
        } else {
            log.info("Swagger is disabled for this profile.");
        }
    }
}


