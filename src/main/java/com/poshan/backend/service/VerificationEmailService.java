package com.poshan.backend.service;

import com.poshan.backend.config.EmailVerificationProperties;
import com.poshan.backend.enums.Role;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VerificationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationEmailService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final EmailVerificationProperties verificationProperties;
    private final String frontendBaseUrl;
    private final String backendBaseUrl;
    private final String mailHost;
    private final String mailUsername;

    public VerificationEmailService(
        ObjectProvider<JavaMailSender> mailSenderProvider,
        EmailVerificationProperties verificationProperties,
        @org.springframework.beans.factory.annotation.Value("${app.frontend-base-url:http://localhost:5173}") String frontendBaseUrl,
        @org.springframework.beans.factory.annotation.Value("${app.backend-base-url:http://localhost:8080}") String backendBaseUrl,
        @org.springframework.beans.factory.annotation.Value("${spring.mail.host:}") String mailHost,
        @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}") String mailUsername
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.verificationProperties = verificationProperties;
        this.frontendBaseUrl = frontendBaseUrl;
        this.backendBaseUrl = backendBaseUrl;
        this.mailHost = mailHost;
        this.mailUsername = mailUsername;
    }

    public void sendVerificationEmail(String email, String name, Role role, String token) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        String fromAddress = resolveFromAddress();

        if (mailSender == null || !StringUtils.hasText(mailHost) || !StringUtils.hasText(fromAddress)) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Email delivery is not configured. Set MAIL_HOST, MAIL_USERNAME, MAIL_PASSWORD, and MAIL_FROM before registering users."
            );
        }

        String safeName = StringUtils.hasText(name) ? name.trim() : "there";
        String roleLabel = role == Role.NUTRITIONIST ? "nutritionist" : "member";
        String verifyUrl = backendBaseUrl.replaceAll("/+$", "")
            + "/api/auth/verify-email-link?token="
            + URLEncoder.encode(token, StandardCharsets.UTF_8);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject("Verify your Poshan email");
        message.setText(
            "Hello " + safeName + ",\n\n"
                + "Please verify your Poshan " + roleLabel + " account by opening this link:\n"
                + verifyUrl + "\n\n"
                + "If you did not create this account, you can ignore this message.\n"
                + "This link expires soon for security reasons."
        );

        try {
            mailSender.send(message);
        } catch (MailException exception) {
            logger.error(
                "Failed to send verification email through host '{}' from '{}' to '{}'",
                mailHost,
                fromAddress,
                email,
                exception
            );
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unable to send the verification email right now. Check your SMTP settings and try again.",
                exception
            );
        }
    }

    private String resolveFromAddress() {
        if (StringUtils.hasText(verificationProperties.getFromAddress())) {
            return verificationProperties.getFromAddress().trim();
        }

        return mailUsername;
    }
}
