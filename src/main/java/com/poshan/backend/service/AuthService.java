package com.poshan.backend.service;

import com.poshan.backend.dto.AuthRequest;
import com.poshan.backend.dto.AuthLoginResponse;
import com.poshan.backend.dto.AuthRegistrationResponse;
import com.poshan.backend.dto.EmailVerificationRequest;
import com.poshan.backend.dto.EmailVerificationResponse;
import com.poshan.backend.dto.MemberRegisterRequest;
import com.poshan.backend.dto.NutritionistRegisterRequest;
import com.poshan.backend.dto.ResendVerificationRequest;
import com.poshan.backend.dto.VerificationStatusResponse;
import com.poshan.backend.config.EmailVerificationProperties;
import com.poshan.backend.entity.AuthToken;
import com.poshan.backend.entity.EmailVerificationToken;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.MemberProfile;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.enums.Role;
import com.poshan.backend.repository.AuthTokenRepository;
import com.poshan.backend.repository.EmailVerificationTokenRepository;
import com.poshan.backend.repository.MemberProfileRepository;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.NutritionistRepository;
import com.poshan.backend.security.JwtService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final NutritionistRepository nutritionistRepository;
    private final AuthTokenRepository authTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final VerificationEmailService verificationEmailService;
    private final EmailVerificationProperties verificationProperties;
    private final MemberProfileRepository memberProfileRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
        MemberRepository memberRepository,
        NutritionistRepository nutritionistRepository,
        AuthTokenRepository authTokenRepository,
        EmailVerificationTokenRepository emailVerificationTokenRepository,
        VerificationEmailService verificationEmailService,
        EmailVerificationProperties verificationProperties,
        MemberProfileRepository memberProfileRepository,
        JwtService jwtService
    ) {
        this.memberRepository = memberRepository;
        this.nutritionistRepository = nutritionistRepository;
        this.authTokenRepository = authTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.verificationEmailService = verificationEmailService;
        this.verificationProperties = verificationProperties;
        this.memberProfileRepository = memberProfileRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthRegistrationResponse registerMember(MemberRegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim().toLowerCase();

        if (memberRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member email already exists");
        }

        if (memberRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Member username already exists");
        }

        Member member = new Member();
        member.setName(request.name());
        member.setUsername(username);
        member.setEmail(email);
        member.setPhone(normalizeOptionalValue(request.phone()));
        member.setPasswordHash(passwordEncoder.encode(request.password()));
        member.setEmailVerified(false);
        member.setEmailVerifiedAt(null);
        member.setLoginCount(0);
        member.setLastLoginAt(null);

        Member savedMember = memberRepository.save(member);
        issueVerificationEmail(savedMember, null, Role.MEMBER);
        return new AuthRegistrationResponse(
            "Verification email sent. Open the link in your inbox before signing in.",
            savedMember.getEmail(),
            Role.MEMBER.name(),
            true,
            false,
            null
        );
    }

    @Transactional
    public AuthLoginResponse loginMember(AuthRequest request) {
        String identifier = request.email().trim();

        Member member = memberRepository.findByEmailIgnoreCase(identifier)
            .or(() -> memberRepository.findByUsernameIgnoreCase(identifier))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid member credentials");
        }

        ensureEmailVerified(member.getEmailVerified());
        member.setLoginCount(member.getLoginCount() + 1);
        member.setLastLoginAt(LocalDateTime.now());
        Member savedMember = memberRepository.save(member);
        AuthToken authToken = issueToken(savedMember, null, Role.MEMBER);
        return toAuthenticatedResponse(savedMember, null, authToken.getToken(), "Signed in successfully.");
    }

    @Transactional
    public AuthRegistrationResponse registerNutritionist(NutritionistRegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim().toLowerCase();

        if (nutritionistRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nutritionist email already exists");
        }

        if (nutritionistRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nutritionist username already exists");
        }

        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setName(request.name());
        nutritionist.setUsername(username);
        nutritionist.setEmail(email);
        nutritionist.setPhone(normalizeOptionalValue(request.phone()));
        nutritionist.setExperience(request.experience());
        nutritionist.setPasswordHash(passwordEncoder.encode(request.password()));
        nutritionist.setSpecialization(request.specialization());
        nutritionist.setEmailVerified(false);
        nutritionist.setEmailVerifiedAt(null);
        nutritionist.setLoginCount(0);
        nutritionist.setLastLoginAt(null);

        Nutritionist savedNutritionist = nutritionistRepository.save(nutritionist);
        issueVerificationEmail(null, savedNutritionist, Role.NUTRITIONIST);
        return new AuthRegistrationResponse(
            "Verification email sent. Open the link in your inbox before signing in.",
            savedNutritionist.getEmail(),
            Role.NUTRITIONIST.name(),
            true,
            false,
            null
        );
    }

    @Transactional
    public AuthLoginResponse loginNutritionist(AuthRequest request) {
        String identifier = request.email().trim();

        Nutritionist nutritionist = nutritionistRepository.findByEmailIgnoreCase(identifier)
            .or(() -> nutritionistRepository.findByUsernameIgnoreCase(identifier))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist not found"));

        if (!passwordEncoder.matches(request.password(), nutritionist.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid nutritionist credentials");
        }

        ensureEmailVerified(nutritionist.getEmailVerified());
        nutritionist.setLoginCount(nutritionist.getLoginCount() + 1);
        nutritionist.setLastLoginAt(LocalDateTime.now());
        Nutritionist savedNutritionist = nutritionistRepository.save(nutritionist);
        AuthToken authToken = issueToken(null, savedNutritionist, Role.NUTRITIONIST);
        return toAuthenticatedResponse(null, savedNutritionist, authToken.getToken(), "Signed in successfully.");
    }

    public void logout(String token) {
        AuthToken authToken = authTokenRepository.findByTokenAndRevokedFalse(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));
        authToken.setRevoked(true);
        authTokenRepository.save(authToken);
    }

    @Transactional
    public EmailVerificationResponse verifyEmail(EmailVerificationRequest request) {
        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(request.token().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Verification link is invalid."));

        if (token.getUsedAt() != null) {
            EmailVerificationResponse alreadyVerifiedResponse = buildAlreadyVerifiedResponse(token);

            if (alreadyVerifiedResponse != null) {
                return alreadyVerifiedResponse;
            }

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This verification link has already been used.");
        }

        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            EmailVerificationResponse alreadyVerifiedResponse = buildAlreadyVerifiedResponse(token);

            if (alreadyVerifiedResponse != null) {
                return alreadyVerifiedResponse;
            }

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This verification link has expired. Request a new one.");
        }

        LocalDateTime verifiedAt = LocalDateTime.now();
        token.setUsedAt(verifiedAt);
        emailVerificationTokenRepository.save(token);

        if (token.getRole() == Role.MEMBER && token.getMember() != null) {
            Member member = token.getMember();
            member.setEmailVerified(true);
            member.setEmailVerifiedAt(verifiedAt);
            member = memberRepository.save(member);
            expirePendingMemberVerificationTokens(member, verifiedAt);
            return issueVerifiedSession(member, null, Role.MEMBER, "Email verified successfully. Signing you in now.");
        }

        if (token.getRole() == Role.NUTRITIONIST && token.getNutritionist() != null) {
            Nutritionist nutritionist = token.getNutritionist();
            nutritionist.setEmailVerified(true);
            nutritionist.setEmailVerifiedAt(verifiedAt);
            nutritionist = nutritionistRepository.save(nutritionist);
            expirePendingNutritionistVerificationTokens(nutritionist, verifiedAt);
            return issueVerifiedSession(null, nutritionist, Role.NUTRITIONIST, "Email verified successfully. Signing you in now.");
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification link is invalid.");
    }

    @Transactional
    public AuthRegistrationResponse resendVerification(ResendVerificationRequest request) {
        Role role = parseRole(request.role());
        String email = request.email().trim().toLowerCase();

        if (role == Role.MEMBER) {
            Member member = memberRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member account not found for that email."));

            if (Boolean.TRUE.equals(member.getEmailVerified())) {
                return new AuthRegistrationResponse(
                    "This email is already verified. You can sign in now.",
                    member.getEmail(),
                    role.name(),
                    false,
                    true,
                    member.getEmailVerifiedAt()
                );
            }

            issueVerificationEmail(member, null, role);
            return new AuthRegistrationResponse(
                "A fresh verification email has been sent.",
                member.getEmail(),
                role.name(),
                true,
                false,
                null
            );
        }

        Nutritionist nutritionist = nutritionistRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist account not found for that email."));

        if (Boolean.TRUE.equals(nutritionist.getEmailVerified())) {
            return new AuthRegistrationResponse(
                "This email is already verified. You can sign in now.",
                nutritionist.getEmail(),
                role.name(),
                false,
                true,
                nutritionist.getEmailVerifiedAt()
            );
        }

        issueVerificationEmail(null, nutritionist, role);
        return new AuthRegistrationResponse(
            "A fresh verification email has been sent.",
            nutritionist.getEmail(),
            role.name(),
            true,
            false,
            null
        );
    }

    @Transactional(readOnly = true)
    public VerificationStatusResponse getVerificationStatus(String emailValue, String roleValue) {
        Role role = parseRole(roleValue);
        String email = emailValue.trim().toLowerCase();

        if (role == Role.MEMBER) {
            Member member = memberRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member account not found for that email."));

            return new VerificationStatusResponse(
                member.getEmail(),
                role.name(),
                Boolean.TRUE.equals(member.getEmailVerified()),
                member.getEmailVerifiedAt()
            );
        }

        Nutritionist nutritionist = nutritionistRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist account not found for that email."));

        return new VerificationStatusResponse(
            nutritionist.getEmail(),
            role.name(),
            Boolean.TRUE.equals(nutritionist.getEmailVerified()),
            nutritionist.getEmailVerifiedAt()
        );
    }

    private AuthToken issueToken(Member member, Nutritionist nutritionist, Role role) {
        LocalDateTime expiresAt = jwtService.calculateAccessTokenExpiry();
        AuthToken authToken = new AuthToken();
        authToken.setToken(jwtService.generateAccessToken(
            member != null ? member.getId() : nutritionist.getId(),
            member != null ? member.getEmail() : nutritionist.getEmail(),
            role,
            expiresAt
        ));
        authToken.setRole(role);
        authToken.setMember(member);
        authToken.setNutritionist(nutritionist);
        authToken.setExpiresAt(expiresAt);
        authToken.setRevoked(false);
        return authTokenRepository.save(authToken);
    }

    private void issueVerificationEmail(Member member, Nutritionist nutritionist, Role role) {
        LocalDateTime now = LocalDateTime.now();

        if (role == Role.MEMBER && member != null) {
            expirePendingMemberVerificationTokens(member, now);
        } else if (role == Role.NUTRITIONIST && nutritionist != null) {
            expirePendingNutritionistVerificationTokens(nutritionist, now);
        }

        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setToken(UUID.randomUUID().toString().replace("-", ""));
        verificationToken.setRole(role);
        verificationToken.setMember(member);
        verificationToken.setNutritionist(nutritionist);
        verificationToken.setExpiresAt(now.plusMinutes(verificationProperties.getTokenTtlMinutes()));
        verificationToken.setUsedAt(null);
        emailVerificationTokenRepository.save(verificationToken);

        if (role == Role.MEMBER && member != null) {
            verificationEmailService.sendVerificationEmail(member.getEmail(), member.getName(), role, verificationToken.getToken());
            return;
        }

        if (role == Role.NUTRITIONIST && nutritionist != null) {
            verificationEmailService.sendVerificationEmail(nutritionist.getEmail(), nutritionist.getName(), role, verificationToken.getToken());
            return;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to issue verification email.");
    }

    private void expirePendingMemberVerificationTokens(Member member, LocalDateTime usedAt) {
        List<EmailVerificationToken> tokens = emailVerificationTokenRepository.findAllByMemberAndUsedAtIsNull(member);
        tokens.forEach(token -> token.setUsedAt(usedAt));
        emailVerificationTokenRepository.saveAll(tokens);
    }

    private void expirePendingNutritionistVerificationTokens(Nutritionist nutritionist, LocalDateTime usedAt) {
        List<EmailVerificationToken> tokens = emailVerificationTokenRepository.findAllByNutritionistAndUsedAtIsNull(nutritionist);
        tokens.forEach(token -> token.setUsedAt(usedAt));
        emailVerificationTokenRepository.saveAll(tokens);
    }

    private void ensureEmailVerified(Boolean emailVerified) {
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Verify your email before signing in. Request a new verification link if you need one."
            );
        }
    }

    private EmailVerificationResponse buildAlreadyVerifiedResponse(EmailVerificationToken token) {
        if (token.getRole() == Role.MEMBER && token.getMember() != null && Boolean.TRUE.equals(token.getMember().getEmailVerified())) {
            return issueVerifiedSession(token.getMember(), null, Role.MEMBER, "This email is already verified. Signing you in now.");
        }

        if (token.getRole() == Role.NUTRITIONIST && token.getNutritionist() != null
            && Boolean.TRUE.equals(token.getNutritionist().getEmailVerified())) {
            return issueVerifiedSession(null, token.getNutritionist(), Role.NUTRITIONIST, "This email is already verified. Signing you in now.");
        }

        return null;
    }

    private EmailVerificationResponse issueVerifiedSession(
        Member member,
        Nutritionist nutritionist,
        Role role,
        String message
    ) {
        LocalDateTime lastLoginAt = LocalDateTime.now();

        if (role == Role.MEMBER && member != null) {
            member.setLoginCount((member.getLoginCount() == null ? 0 : member.getLoginCount()) + 1);
            member.setLastLoginAt(lastLoginAt);
            Member savedMember = memberRepository.save(member);
            AuthToken authToken = issueToken(savedMember, null, role);
            return toEmailVerificationResponse(toAuthenticatedResponse(savedMember, null, authToken.getToken(), message));
        }

        if (role == Role.NUTRITIONIST && nutritionist != null) {
            nutritionist.setLoginCount((nutritionist.getLoginCount() == null ? 0 : nutritionist.getLoginCount()) + 1);
            nutritionist.setLastLoginAt(lastLoginAt);
            Nutritionist savedNutritionist = nutritionistRepository.save(nutritionist);
            AuthToken authToken = issueToken(null, savedNutritionist, role);
            return toEmailVerificationResponse(toAuthenticatedResponse(null, savedNutritionist, authToken.getToken(), message));
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification link is invalid.");
    }

    private EmailVerificationResponse toEmailVerificationResponse(AuthLoginResponse response) {
        return new EmailVerificationResponse(
            response.message(),
            response.email(),
            response.role(),
            response.emailVerified(),
            response.emailVerifiedAt(),
            response.id(),
            response.name(),
            response.username(),
            response.phone(),
            response.specialization(),
            response.experience(),
            response.age(),
            response.loginCount(),
            response.accessToken(),
            response.profileCompleted()
        );
    }

    private Role parseRole(String roleValue) {
        try {
            return Role.valueOf(roleValue.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be MEMBER or NUTRITIONIST.");
        }
    }

    private AuthLoginResponse toAuthenticatedResponse(
        Member member,
        Nutritionist nutritionist,
        String accessToken,
        String message
    ) {
        if (member != null) {
            MemberProfile profile = memberProfileRepository.findByMemberId(member.getId()).orElse(null);
            Integer age = profile != null ? profile.getAge() : null;

            return new AuthLoginResponse(
                member.getId(),
                member.getName(),
                member.getUsername(),
                member.getEmail(),
                member.getPhone(),
                member.getRole().name(),
                null,
                null,
                age,
                member.getLoginCount(),
                accessToken,
                Boolean.TRUE.equals(member.getEmailVerified()),
                member.getEmailVerifiedAt(),
                age != null,
                message
            );
        }

        return new AuthLoginResponse(
            nutritionist.getId(),
            nutritionist.getName(),
            nutritionist.getUsername(),
            nutritionist.getEmail(),
            nutritionist.getPhone(),
            nutritionist.getRole().name(),
            nutritionist.getSpecialization(),
            nutritionist.getExperience(),
            nutritionist.getAge(),
            nutritionist.getLoginCount(),
            accessToken,
            Boolean.TRUE.equals(nutritionist.getEmailVerified()),
            nutritionist.getEmailVerifiedAt(),
            nutritionist.getAge() != null,
            message
        );
    }

    private String normalizeOptionalValue(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
