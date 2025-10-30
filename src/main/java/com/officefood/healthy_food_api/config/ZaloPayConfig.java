package com.officefood.healthy_food_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "zalopay")
@Data
public class ZaloPayConfig {
    private String appId;
    private String key1;
    private String key2;
    private String endpoint;
    private String callbackUrl;
    private String redirectUrl;

    // Default values for testing
    public String getAppId() {
        return appId != null ? appId : "2553";
    }

    public String getKey1() {
        return key1 != null ? key1 : "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    }

    public String getKey2() {
        return key2 != null ? key2 : "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz";
    }

    public String getEndpoint() {
        return endpoint != null ? endpoint : "https://sb-openapi.zalopay.vn/v2/create";
    }

    public String getCallbackUrl() {
        return callbackUrl != null ? callbackUrl : "http://localhost:8080/api/zalopay/callback";
    }

    public String getRedirectUrl() {
        return redirectUrl != null ? redirectUrl : "http://localhost:3000/payment/result";
    }
}

