package com.btpl.physio.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String to, String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            // Email failed (e.g. SMTP not configured). Print so we can still test.
            System.out.println("EMAIL FAILED: " + e.getMessage());
            System.out.println("TO: " + to + " | SUBJECT: " + subject);
            System.out.println(body);
        }
    }
    
    public void sendBookingConfirmation(String to, String subject, String details) {
        send(to, subject, details);
    }

    public void sendVerificationEmail(String to, String link) {
        send(to, "Verify your email",
                "Welcome! Please verify your email by clicking this link:\n" + link);
    }
}