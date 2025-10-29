package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.service.OtpService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * OTP Service Implementation
 * Generates and validates 6-digit OTP codes
 */
@Service
public class OtpServiceImpl implements OtpService {
    private static final SecureRandom random = new SecureRandom();
    
    @Override
    public String generateOtp() {
        // Generate 6-digit OTP (100000 to 999999)
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    @Override
    public boolean validateOtp(String inputOtp, String storedOtp) {
        if (inputOtp == null || storedOtp == null) {
            return false;
        }
        // Trim whitespace and compare
        return inputOtp.trim().equals(storedOtp.trim());
    }
}
