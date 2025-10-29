package com.officefood.healthy_food_api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimezoneConfig {

    public static final ZoneId VIETNAM_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");

    @PostConstruct
    public void init() {
        // Cấu hình timezone mặc định cho JVM là GMT+7 (Asia/Ho_Chi_Minh)
        TimeZone.setDefault(TimeZone.getTimeZone(VIETNAM_ZONE_ID));
    }
}

