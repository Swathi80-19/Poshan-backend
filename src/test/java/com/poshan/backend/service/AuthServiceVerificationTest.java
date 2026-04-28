package com.poshan.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.poshan.backend.config.EmailVerificationProperties;
import com.poshan.backend.dto.EmailVerificationRequest;
import com.poshan.backend.dto.EmailVerificationResponse;
import com.poshan.backend.entity.EmailVerificationToken;
import com.poshan.backend.entity.Member;
import com.poshan.backend.enums.Role;
import com.poshan.backend.repository.AuthTokenRepository;
import com.poshan.backend.repository.EmailVerificationTokenRepository;
import com.poshan.backend.repository.MemberProfileRepository;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.NutritionistRepository;
import com.poshan.backend.repository.PhoneLoginChallengeRepository;
import com.poshan.backend.security.JwtService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceVerificationTest {

    private MemberRepository memberRepository;
    private NutritionistRepository nutritionistRepository;
    private AuthTokenRepository authTokenRepository;
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    private MemberProfileRepository memberProfileRepository;
    private PhoneLoginChallengeRepository phoneLoginChallengeRepository;
    private VerificationEmailService verificationEmailService;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        nutritionistRepository = mock(NutritionistRepository.class);
        authTokenRepository = mock(AuthTokenRepository.class);
        emailVerificationTokenRepository = mock(EmailVerificationTokenRepository.class);
        memberProfileRepository = mock(MemberProfileRepository.class);
        phoneLoginChallengeRepository = mock(PhoneLoginChallengeRepository.class);
        verificationEmailService = mock(VerificationEmailService.class);
        jwtService = new JwtService("poshan-test-jwt-secret-change-this-before-production-123456789", 7);
        jwtService.initialize();

        EmailVerificationProperties verificationProperties = new EmailVerificationProperties();
        verificationProperties.setTokenTtlMinutes(30);

        authService = new AuthService(
            memberRepository,
            nutritionistRepository,
            authTokenRepository,
            emailVerificationTokenRepository,
            verificationEmailService,
            verificationProperties,
            memberProfileRepository,
            phoneLoginChallengeRepository,
            jwtService,
            10,
            true
        );
    }

    @Test
    void verifyEmailReturnsSuccessWhenUsedTokenBelongsToVerifiedMember() {
        LocalDateTime verifiedAt = LocalDateTime.of(2026, 4, 26, 9, 30);

        Member member = new Member();
        member.setEmail("member@example.com");
        member.setEmailVerified(true);
        member.setEmailVerifiedAt(verifiedAt);

        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken("used-token");
        token.setRole(Role.MEMBER);
        token.setMember(member);
        token.setUsedAt(verifiedAt.minusMinutes(1));
        token.setExpiresAt(verifiedAt.plusMinutes(29));

        when(emailVerificationTokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        EmailVerificationResponse response = authService.verifyEmail(new EmailVerificationRequest("used-token"));

        assertEquals("This email is already verified. You can sign in now.", response.message());
        assertEquals("member@example.com", response.email());
        assertEquals("MEMBER", response.role());
        assertTrue(response.verified());
        assertEquals(verifiedAt, response.verifiedAt());
        verify(memberRepository, never()).save(member);
    }
}
