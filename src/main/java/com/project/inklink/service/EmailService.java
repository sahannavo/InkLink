package com.project.inklink.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private JavaMailSender mailSender;
    private TemplateEngine templateEngine;

    // Use @Autowired(required = false) to make dependencies optional
    @Autowired(required = false)
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Autowired(required = false)
    public void setTemplateEngine(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void sendWelcomeEmail(String to, String username) {
        if (mailSender == null || templateEngine == null) {
            System.out.println("Email service not configured. Would send welcome email to: " + to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Welcome to InkLink - Start Sharing Your Stories!");
            helper.setFrom("noreply@inklink.com");

            // Prepare the Thymeleaf context
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("welcomeMessage", "We're excited to have you join our community of storytellers!");

            // Process the HTML template
            String htmlContent = templateEngine.process("email/welcome-email", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    public void sendSimpleMessage(String to, String subject, String text) {
        if (mailSender == null) {
            System.out.println("Email service not configured. Would send message to: " + to);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("noreply@inklink.com");

        mailSender.send(message);
    }
}