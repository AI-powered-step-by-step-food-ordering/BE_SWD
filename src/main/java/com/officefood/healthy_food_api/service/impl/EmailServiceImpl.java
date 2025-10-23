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
    public boolean sendVerificationEmail(String customerName, String customerEmail, String verificationToken) {
        try {
            log.info("🔄 Starting sendVerificationEmail process...");
            log.info("📧 Customer Email: {}", customerEmail);
            log.info("👤 Customer Name: {}", customerName);
            log.info("🔑 Verification Token: {}", verificationToken);
            log.info("🌐 Website URL: {}", websiteUrl);
            log.info("📨 From Email: {}", fromEmail);

            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerName", customerName);
            variables.put("verificationToken", verificationToken);
            variables.put("websiteUrl", websiteUrl);
            variables.put("companyName", companyName);
            variables.put("supportEmail", supportEmail);
            variables.put("currentYear", LocalDateTime.now().getYear());

            // Create verification link
            String verificationLink = websiteUrl + "/api/auth/verify-email?token=" + verificationToken;
            variables.put("verificationLink", verificationLink);

            // Send template email
            boolean result = sendTemplateEmail(
                customerEmail,
                "Verify Your Email - " + companyName,
                "email/verification",
                variables
            );

            log.info("📬 Template email send result: {}", result);
            return result;

        } catch (Exception e) {
            log.error("❌ Exception in sendVerificationEmail: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendWelcomeEmail(String customerName, String customerEmail) {
        try {
            log.info("🔄 Starting sendWelcomeEmail process...");
            log.info("📧 Customer Email: {}", customerEmail);
            log.info("👤 Customer Name: {}", customerName);

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

            log.info("📬 Welcome email send result: {}", result);
            return result;

        } catch (Exception e) {
            log.error("❌ Exception in sendWelcomeEmail: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendPasswordResetEmail(String email, String resetToken) {
        try {
            log.info("🔄 Starting sendPasswordResetEmail process...");
            log.info("📧 Email: {}", email);
            log.info("🔑 Reset Token: {}", resetToken);

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

            log.info("📬 Password reset email send result: {}", result);
            return result;

        } catch (Exception e) {
            log.error("❌ Exception in sendPasswordResetEmail: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendPlainEmail(String to, String subject, String content) {
        try {
            log.info("🔄 Sending plain email to: {}", to);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("✅ Plain email sent successfully to: {}", to);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Failed to send plain email to: {} - {}", to, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            log.info("🔄 Sending HTML email to: {}", to);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("✅ HTML email sent successfully to: {}", to);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Failed to send HTML email to: {} - {}", to, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send template email using Thymeleaf
     */
    private boolean sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            log.info("🔄 Sending template email to: {} using template: {}", to, templateName);
            
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
            log.info("✅ Template email sent successfully to: {}", to);
            return true;
            
        } catch (Exception e) {
            log.error("❌ Failed to send template email to: {} - {}", to, e.getMessage(), e);
            return false;
        }
    }
}
