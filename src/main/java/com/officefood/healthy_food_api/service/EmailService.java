package com.officefood.healthy_food_api.service;

/**
 * Email Service Interface
 * Handles email notifications and communications
 * 
 * @author Healthy Food API
 * @version 1.0.0
 */
public interface EmailService {

    /**
     * Send email verification email
     * 
     * @param customerName      customer name
     * @param customerEmail     customer email
     * @param verificationToken verification token
     * @return true if email sent successfully
     */
    boolean sendVerificationEmail(String customerName, String customerEmail, String verificationToken);

    /**
     * Send welcome email for new customers
     * 
     * @param customerName  customer name
     * @param customerEmail customer email
     * @return true if email sent successfully
     */
    boolean sendWelcomeEmail(String customerName, String customerEmail);

    /**
     * Send password reset email
     * 
     * @param email      customer email
     * @param resetToken reset token
     * @return true if email sent successfully
     */
    boolean sendPasswordResetEmail(String email, String resetToken);

    /**
     * Send plain text email
     * 
     * @param to      recipient email
     * @param subject email subject
     * @param content email content
     * @return true if email sent successfully
     */
    boolean sendPlainEmail(String to, String subject, String content);

    /**
     * Send HTML email
     * 
     * @param to          recipient email
     * @param subject     email subject
     * @param htmlContent HTML content
     * @return true if email sent successfully
     */
    boolean sendHtmlEmail(String to, String subject, String htmlContent);
}
