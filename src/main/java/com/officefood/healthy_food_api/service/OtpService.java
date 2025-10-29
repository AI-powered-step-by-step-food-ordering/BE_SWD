package com.officefood.healthy_food_api.service;

/**
 * OTP Service - Generate and validate OTP codes
 */
public interface OtpService {
    
    /**
     * Generate a 6-digit OTP code
     * @return 6-digit OTP string
     */
    String generateOtp();
    
    /**
     * Validate OTP (trim whitespace)
     * @param inputOtp OTP from user input
     * @param storedOtp OTP stored in database
     * @return true if OTP matches
     */
    boolean validateOtp(String inputOtp, String storedOtp);
}
