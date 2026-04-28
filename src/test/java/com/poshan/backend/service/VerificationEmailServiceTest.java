package com.poshan.backend.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.poshan.backend.config.EmailVerificationProperties;
import com.poshan.backend.enums.Role;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class VerificationEmailServiceTest {

    @Test
    void sendVerificationEmailUsesFrontendVerificationPageLink() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailVerificationProperties properties = new EmailVerificationProperties();
        properties.setFromAddress("noreply@poshan.app");

        VerificationEmailService service = new VerificationEmailService(
            new StaticListableBeanFactory(Map.of("mailSender", mailSender)).getBeanProvider(JavaMailSender.class),
            properties,
            "https://poshn.vercel.app/",
            "smtp.gmail.com",
            "noreply@poshan.app"
        );

        service.sendVerificationEmail("member@example.com", "Member User", Role.MEMBER, "abc123token");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        String messageBody = messageCaptor.getValue().getText();
        assertTrue(messageBody.contains("https://poshn.vercel.app/verify-email?token=abc123token"));
        assertTrue(messageBody.contains("&email=member%40example.com"));
        assertTrue(messageBody.contains("&role=MEMBER"));
    }
}
