package com.officefood.healthy_food_api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config.file:classpath:firebase-service-account.json}")
    private Resource firebaseConfigResource;

    // Fallback config files to try in order
    private static final String[] FALLBACK_CONFIG_FILES = {
            "classpath:firebase-service-account.json",
            "file:firebase-service-account.json",
            "file:src/main/resources/firebase-service-account.json"
    };

    @PostConstruct
    public void initialize() {
        // Try to initialize with the configured resource first
        if (tryInitializeWithResource(firebaseConfigResource, "configured resource")) {
            return;
        }

        // Try fallback locations
        log.warn("⚠️ Primary Firebase config not found, trying fallback locations...");
        for (String location : FALLBACK_CONFIG_FILES) {
            try {
                Resource resource = new org.springframework.core.io.DefaultResourceLoader().getResource(location);
                if (tryInitializeWithResource(resource, location)) {
                    return;
                }
            } catch (Exception e) {
                log.debug("Could not load Firebase config from: {}", location);
            }
        }

        // If all attempts failed
        log.error("❌ Failed to initialize Firebase Admin SDK - No valid config file found!");
        log.error("Tried the following locations:");
        for (String location : FALLBACK_CONFIG_FILES) {
            log.error("  - {}", location);
        }
        log.warn("⚠️ Push notifications will be DISABLED until Firebase is properly configured.");
        log.warn("📝 Please add one of these files:");
        log.warn("   1. src/main/resources/firebase-service-account.json (recommended)");
        log.warn("   2. src/main/resources/firebasekeyswd.json (alternative name)");
        log.warn("   3. Download from Firebase Console → Project Settings → Service Accounts");
    }

    private boolean tryInitializeWithResource(Resource resource, String location) {
        try {
            if (resource != null && resource.exists()) {
                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("✅ Firebase Admin SDK initialized successfully from: {}", location);
                    log.info("📱 Push notifications are ENABLED");
                    return true;
                } else {
                    log.info("Firebase App already initialized");
                    return true;
                }
            }
        } catch (IOException e) {
            log.debug("Failed to initialize Firebase from {}: {}", location, e.getMessage());
        }
        return false;
    }
}

