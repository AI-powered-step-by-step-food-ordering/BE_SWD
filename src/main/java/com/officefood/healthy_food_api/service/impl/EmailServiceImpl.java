package com.officefood.healthy_food_api.service.impl;

import com.officefood.healthy_food_api.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Email Service Implementation
 * Handles email notifications and communications
 * 
 * @author Healthy Food API
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:healthyfoodapi@gmail.com}")
    private String fromEmail;

    @Value("${app.email.website-url:http://localhost:8080}")
    private String websiteUrl;

    @Value("${app.email.company-name:Healthy Food API}")
    private String companyName;

    @Value("${app.email.support-email:healthyfoodapi@gmail.com}")
    private String supportEmail;

    @Override
    public boolean sendVerificationOtpEmail(String customerName, String customerEmail, String verificationOtp) {
        try {
            log.info("Ã°Å¸â€â€ž Starting sendVerificationOtpEmail process...");
            log.info("Ã°Å¸â€œÂ§ Customer Email: {}", customerEmail);
            log.info("Ã°Å¸â€˜Â¤ Customer Name: {}", customerName);
            log.info("Ã°Å¸â€Â¢ OTP: {}", verificationOtp);
            log.info("Ã°Å¸Å’Â Website URL: {}", websiteUrl);
            log.info("Ã°Å¸â€œÂ¨ From Email: {}", fromEmail);

            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customerName);
            variables.put("verificationOtp", verificationOtp);
            variables.put("otpExpiryMinutes", 10);
            variables.put("websiteUrl", websiteUrl);
            variables.put("companyName", companyName);
            variables.put("supportEmail", supportEmail);
            variables.put("currentYear", LocalDateTime.now().getYear());

            boolean result = sendTemplateEmail(
                customerEmail,
                "Verify Your Email - " + companyName,
                "email/verification-otp",
                variables
            );

            log.info("Ã°Å¸â€œÂ¬ OTP email send result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ Exception in sendVerificationOtpEmail: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendPasswordResetOtpEmail(String customerName, String customerEmail, String otp) {
        try {
            log.info("Ã°Å¸â€â€ž Starting sendPasswordResetOtpEmail process...");
            log.info("Ã°Å¸â€œÂ§ Customer Email: {}", customerEmail);
            log.info("Ã°Å¸â€˜Â¤ Customer Name: {}", customerName);
            log.info("Ã°Å¸â€Â¢ OTP: {}", otp);

            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customerName);
            variables.put("otp", otp);
            variables.put("otpExpiryMinutes", 10);
            variables.put("websiteUrl", websiteUrl);
            variables.put("companyName", companyName);
            variables.put("supportEmail", supportEmail);
            variables.put("currentYear", LocalDateTime.now().getYear());

            boolean result = sendTemplateEmail(
                    customerEmail,
                    "Reset Your Password - " + companyName,
                    "email/password-reset-otp",
                    variables
            );
            log.info("Ã°Å¸â€œÂ¬ Password reset OTP email send result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ Exception in sendPasswordResetOtpEmail: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendWelcomeEmail(String customerName, String customerEmail) {
        try {
            log.info("Ã°Å¸â€â€ž Starting sendWelcomeEmail process...");
            log.info("Ã°Å¸â€œÂ§ Customer Email: {}", customerEmail);
            log.info("Ã°Å¸â€˜Â¤ Customer Name: {}", customerName);

            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customerName);
            variables.put("websiteUrl", websiteUrl);
            variables.put("companyName", companyName);
            variables.put("supportEmail", supportEmail);
            variables.put("currentYear", LocalDateTime.now().getYear());

            // Send template email
            boolean result = sendTemplateEmail(
                customerEmail,
                "Welcome to " + companyName + "!",
                "email/welcome",
                variables
            );

            log.info("Ã°Å¸â€œÂ¬ Welcome email send result: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ Exception in sendWelcomeEmail: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendPasswordResetEmail(String email, String resetToken) {
        try {
            log.info("Ã°Å¸â€â€ž Starting sendPasswordResetEmail process...");
            log.info("Ã°Å¸â€œÂ§ Email: {}", email);
            log.info("Ã°Å¸â€â€˜ Reset Token: {}", resetToken);

            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("resetToken", resetToken);
            variables.put("websiteUrl", websiteUrl);
            variables.put("companyName", companyName);
            variables.put("supportEmail", supportEmail);
            variables.put("currentYear", LocalDateTime.now().getYear());

            // Create reset link
            String resetLink = websiteUrl + "/api/auth/reset-password?token=" + resetToken;
            variables.put("resetLink", resetLink);

            // Send template email
            boolean result = sendTemplateEmail(
                email,
                "Password Reset - " + companyName,
                "email/password-reset",
                variables
            );

            log.info("Ã°Å¸â€œÂ¬ Password reset email send result: {}", result);
            return result;

        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ Exception in sendPasswordResetEmail: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendPlainEmail(String to, String subject, String content) {
        try {
            log.info("Ã°Å¸â€â€ž Sending plain email to: {}", to);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Ã¢Å“â€¦ Plain email sent successfully to: {}", to);
            return true;
            
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ Failed to send plain email to: {} - {}", to, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            log.info("Ã°Å¸â€â€ž Sending HTML email to: {}", to);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Ã¢Å“â€¦ HTML email sent successfully to: {}", to);
            return true;
            
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ Failed to send HTML email to: {} - {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send template email using Thymeleaf
     */
    private boolean sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            log.info("Ã°Å¸â€â€ž Sending template email to: {} using template: {}", to, templateName);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Process template
            Context context = new Context();
            variables.forEach(context::setVariable);
            
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Ã¢Å“â€¦ Template email sent successfully to: {}", to);
            return true;
            
        } catch (Exception e) {
            log.error("Ã¢ÂÅ’ Failed to send template email to: {} - {}", to, e.getMessage(), e);
            return false;
        }
    }
}
