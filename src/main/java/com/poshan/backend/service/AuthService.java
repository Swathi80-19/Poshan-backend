package com.poshan.backend.service;

import com.poshan.backend.dto.AuthRequest;
import com.poshan.backend.dto.AuthLoginResponse;
import com.poshan.backend.dto.AuthRegistrationResponse;
import com.poshan.backend.dto.EmailVerificationRequest;
import com.poshan.backend.dto.EmailVerificationResponse;
import com.poshan.backend.dto.MemberRegisterRequest;
import com.poshan.backend.dto.NutritionistRegisterRequest;
import com.poshan.backend.dto.PhoneOtpResendRequest;
import com.poshan.backend.dto.PhoneOtpVerifyRequest;
import com.poshan.backend.dto.ResendVerificationRequest;
import com.poshan.backend.dto.VerificationStatusResponse;
import com.poshan.backend.config.EmailVerificationProperties;
import com.poshan.backend.entity.AuthToken;
import com.poshan.backend.entity.EmailVerificationToken;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.MemberProfile;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.entity.PhoneLoginChallenge;
import com.poshan.backend.enums.Role;
import com.poshan.backend.repository.AuthTokenRepository;
import com.poshan.backend.repository.EmailVerificationTokenRepository;
import com.poshan.backend.repository.MemberProfileRepository;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.NutritionistRepository;
import com.poshan.backend.repository.PhoneLoginChallengeRepository;
import com.poshan.backend.security.JwtService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    private final PhoneLoginChallengeRepository phoneLoginChallengeRepository;
    private final JwtService jwtService;
    private final int phoneOtpTtlMinutes;
    private final boolean exposePhoneOtpInResponse;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
        MemberRepository memberRepository,
        NutritionistRepository nutritionistRepository,
        AuthTokenRepository authTokenRepository,
        EmailVerificationTokenRepository emailVerificationTokenRepository,
        VerificationEmailService verificationEmailService,
        EmailVerificationProperties verificationProperties,
        MemberProfileRepository memberProfileRepository,
        PhoneLoginChallengeRepository phoneLoginChallengeRepository,
        JwtService jwtService,
        @org.springframework.beans.factory.annotation.Value("${app.auth.phone-otp.token-ttl-minutes:10}") int phoneOtpTtlMinutes,
        @org.springframework.beans.factory.annotation.Value("${app.auth.phone-otp.expose-code-in-response:true}") boolean exposePhoneOtpInResponse
    ) {
        this.memberRepository = memberRepository;
        this.nutritionistRepository = nutritionistRepository;
        this.authTokenRepository = authTokenRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.verificationEmailService = verificationEmailService;
        this.verificationProperties = verificationProperties;
        this.memberProfileRepository = memberProfileRepository;
        this.phoneLoginChallengeRepository = phoneLoginChallengeRepository;
        this.jwtService = jwtService;
        this.phoneOtpTtlMinutes = phoneOtpTtlMinutes;
        this.exposePhoneOtpInResponse = exposePhoneOtpInResponse;
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
        requirePhone(member.getPhone(), Role.MEMBER);
        PhoneLoginChallenge challenge = issuePhoneChallenge(member, null, Role.MEMBER);
        return toPhoneChallengeResponse(member, null, challenge, "Verify the code sent to your phone to finish signing in.");
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
        requirePhone(nutritionist.getPhone(), Role.NUTRITIONIST);
        PhoneLoginChallenge challenge = issuePhoneChallenge(null, nutritionist, Role.NUTRITIONIST);
        return toPhoneChallengeResponse(
            null,
            nutritionist,
            challenge,
            "Verify the code sent to your phone to finish signing in."
        );
    }

    public void logout(String token) {
        AuthToken authToken = authTokenRepository.findByTokenAndRevokedFalse(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));
        authToken.setRevoked(true);
        authTokenRepository.save(authToken);
    }

    @Transactional
    public AuthLoginResponse verifyPhoneOtp(PhoneOtpVerifyRequest request) {
        PhoneLoginChallenge challenge = phoneLoginChallengeRepository.findByChallengeId(request.challengeId().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phone verification challenge not found."));

        if (challenge.getUsedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This phone verification code has already been used.");
        }

        if (challenge.getExpiresAt() == null || challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This phone verification code has expired. Request a new one.");
        }

        if (!challenge.getOtpCode().equals(request.otp().trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone verification code.");
        }

        challenge.setUsedAt(LocalDateTime.now());
        phoneLoginChallengeRepository.save(challenge);

        if (challenge.getRole() == Role.MEMBER && challenge.getMember() != null) {
            Member member = challenge.getMember();
            member.setLoginCount(member.getLoginCount() + 1);
            member.setLastLoginAt(LocalDateTime.now());
            Member savedMember = memberRepository.save(member);
            AuthToken authToken = issueToken(savedMember, null, Role.MEMBER);
            return toAuthenticatedResponse(savedMember, null, authToken.getToken(), "Signed in successfully.");
        }

        if (challenge.getRole() == Role.NUTRITIONIST && challenge.getNutritionist() != null) {
            Nutritionist nutritionist = challenge.getNutritionist();
            nutritionist.setLoginCount(nutritionist.getLoginCount() + 1);
            nutritionist.setLastLoginAt(LocalDateTime.now());
            Nutritionist savedNutritionist = nutritionistRepository.save(nutritionist);
            AuthToken authToken = issueToken(null, savedNutritionist, Role.NUTRITIONIST);
            return toAuthenticatedResponse(null, savedNutritionist, authToken.getToken(), "Signed in successfully.");
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone verification challenge is invalid.");
    }

    @Transactional
    public AuthLoginResponse resendPhoneOtp(PhoneOtpResendRequest request) {
        PhoneLoginChallenge challenge = phoneLoginChallengeRepository.findByChallengeId(request.challengeId().trim())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Phone verification challenge not found."));

        if (challenge.getRole() == Role.MEMBER && challenge.getMember() != null) {
            PhoneLoginChallenge nextChallenge = issuePhoneChallenge(challenge.getMember(), null, Role.MEMBER);
            return toPhoneChallengeResponse(
                challenge.getMember(),
                null,
                nextChallenge,
                "A fresh verification code has been sent to your phone."
            );
        }

        if (challenge.getRole() == Role.NUTRITIONIST && challenge.getNutritionist() != null) {
            PhoneLoginChallenge nextChallenge = issuePhoneChallenge(null, challenge.getNutritionist(), Role.NUTRITIONIST);
            return toPhoneChallengeResponse(
                null,
                challenge.getNutritionist(),
                nextChallenge,
                "A fresh verification code has been sent to your phone."
            );
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone verification challenge is invalid.");
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
            memberRepository.save(member);
            expirePendingMemberVerificationTokens(member, verifiedAt);
            return new EmailVerificationResponse(
                "Email verified successfully. You can now sign in to your member account.",
                member.getEmail(),
                Role.MEMBER.name(),
                true,
                member.getEmailVerifiedAt()
            );
        }

        if (token.getRole() == Role.NUTRITIONIST && token.getNutritionist() != null) {
            Nutritionist nutritionist = token.getNutritionist();
            nutritionist.setEmailVerified(true);
            nutritionist.setEmailVerifiedAt(verifiedAt);
            nutritionistRepository.save(nutritionist);
            expirePendingNutritionistVerificationTokens(nutritionist, verifiedAt);
            return new EmailVerificationResponse(
                "Email verified successfully. You can now sign in to your nutritionist account.",
                nutritionist.getEmail(),
                Role.NUTRITIONIST.name(),
                true,
                nutritionist.getEmailVerifiedAt()
            );
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

    private PhoneLoginChallenge issuePhoneChallenge(Member member, Nutritionist nutritionist, Role role) {
        LocalDateTime now = LocalDateTime.now();

        if (role == Role.MEMBER && member != null) {
            expirePendingPhoneChallengesForMember(member, now);
        } else if (role == Role.NUTRITIONIST && nutritionist != null) {
            expirePendingPhoneChallengesForNutritionist(nutritionist, now);
        }

        PhoneLoginChallenge challenge = new PhoneLoginChallenge();
        challenge.setChallengeId(UUID.randomUUID().toString().replace("-", ""));
        challenge.setOtpCode(generateOtp());
        challenge.setPhone(role == Role.MEMBER ? member.getPhone() : nutritionist.getPhone());
        challenge.setRole(role);
        challenge.setMember(member);
        challenge.setNutritionist(nutritionist);
        challenge.setExpiresAt(now.plusMinutes(phoneOtpTtlMinutes));
        challenge.setUsedAt(null);
        return phoneLoginChallengeRepository.save(challenge);
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

    private void expirePendingPhoneChallengesForMember(Member member, LocalDateTime usedAt) {
        List<PhoneLoginChallenge> challenges = phoneLoginChallengeRepository.findAllByMemberAndUsedAtIsNull(member);
        challenges.forEach(challenge -> challenge.setUsedAt(usedAt));
        phoneLoginChallengeRepository.saveAll(challenges);
    }

    private void expirePendingPhoneChallengesForNutritionist(Nutritionist nutritionist, LocalDateTime usedAt) {
        List<PhoneLoginChallenge> challenges = phoneLoginChallengeRepository.findAllByNutritionistAndUsedAtIsNull(nutritionist);
        challenges.forEach(challenge -> challenge.setUsedAt(usedAt));
        phoneLoginChallengeRepository.saveAll(challenges);
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
            Member member = token.getMember();
            return new EmailVerificationResponse(
                "This email is already verified. You can sign in now.",
                member.getEmail(),
                Role.MEMBER.name(),
                true,
                member.getEmailVerifiedAt()
            );
        }

        if (token.getRole() == Role.NUTRITIONIST && token.getNutritionist() != null
            && Boolean.TRUE.equals(token.getNutritionist().getEmailVerified())) {
            Nutritionist nutritionist = token.getNutritionist();
            return new EmailVerificationResponse(
                "This email is already verified. You can sign in now.",
                nutritionist.getEmail(),
                Role.NUTRITIONIST.name(),
                true,
                nutritionist.getEmailVerifiedAt()
            );
        }

        return null;
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
                false,
                null,
                maskPhone(member.getPhone()),
                null,
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
            false,
            null,
            maskPhone(nutritionist.getPhone()),
            null,
            message
        );
    }

    private AuthLoginResponse toPhoneChallengeResponse(
        Member member,
        Nutritionist nutritionist,
        PhoneLoginChallenge challenge,
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
                null,
                Boolean.TRUE.equals(member.getEmailVerified()),
                member.getEmailVerifiedAt(),
                age != null,
                true,
                challenge.getChallengeId(),
                maskPhone(member.getPhone()),
                exposePhoneOtpInResponse ? challenge.getOtpCode() : null,
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
            null,
            Boolean.TRUE.equals(nutritionist.getEmailVerified()),
            nutritionist.getEmailVerifiedAt(),
            nutritionist.getAge() != null,
            true,
            challenge.getChallengeId(),
            maskPhone(nutritionist.getPhone()),
            exposePhoneOtpInResponse ? challenge.getOtpCode() : null,
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

    private void requirePhone(String phone, Role role) {
        if (StringUtils.hasText(phone)) {
            return;
        }

        String roleLabel = role == Role.NUTRITIONIST ? "nutritionist" : "member";
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "A phone number is required on the " + roleLabel + " account before phone verification can be used."
        );
    }

    private String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    private String maskPhone(String phone) {
        String digitsOnly = phone == null ? "" : phone.replaceAll("\\s+", "");

        if (digitsOnly.length() <= 4) {
            return digitsOnly;
        }

        return "*".repeat(Math.max(digitsOnly.length() - 4, 0)) + digitsOnly.substring(digitsOnly.length() - 4);
    }
}
