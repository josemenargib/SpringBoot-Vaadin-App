package com.primefactorsolutions.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmailService {
    public static final String NO_REPLY_PRIMEFACTORSOLUTIONS_COM = "no-reply@primefactorsolutions.com";
    private final JavaMailSender emailSender;

    public void sendEmail(final String email, final String title, final String messageContent) {
        try {
            final SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(NO_REPLY_PRIMEFACTORSOLUTIONS_COM);
            message.setBcc(NO_REPLY_PRIMEFACTORSOLUTIONS_COM);
            message.setTo(email);
            message.setSubject(title);
            message.setText(messageContent);

            emailSender.send(message);
            log.info("Sent email to {}", email);
        } catch (Exception e) {
            log.error("Error sending email to {}", email, e);
            throw e;
        }
    }
}
