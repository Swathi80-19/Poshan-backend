package com.poshan.backend.service;

import com.poshan.backend.dto.AuthRequest;
import com.poshan.backend.dto.MemberRegisterRequest;
import com.poshan.backend.dto.NutritionistRegisterRequest;
import com.poshan.backend.dto.SessionResponse;
import com.poshan.backend.entity.AuthToken;
import com.poshan.backend.entity.Member;
import com.poshan.backend.entity.Nutritionist;
import com.poshan.backend.enums.Role;
import com.poshan.backend.repository.AuthTokenRepository;
import com.poshan.backend.repository.MemberRepository;
import com.poshan.backend.repository.NutritionistRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final NutritionistRepository nutritionistRepository;
    private final AuthTokenRepository authTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
        MemberRepository memberRepository,
        NutritionistRepository nutritionistRepository,
        AuthTokenRepository authTokenRepository
    ) {
        this.memberRepository = memberRepository;
        this.nutritionistRepository = nutritionistRepository;
        this.authTokenRepository = authTokenRepository;
    }

    public SessionResponse registerMember(MemberRegisterRequest request) {
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
        member.setPasswordHash(passwordEncoder.encode(request.password()));
        member.setLoginCount(0);
        member.setLastLoginAt(null);

        return toSessionResponse(memberRepository.save(member), null, null);
    }

    public SessionResponse loginMember(AuthRequest request) {
        String identifier = request.email().trim();

        Member member = memberRepository.findByEmailIgnoreCase(identifier)
            .or(() -> memberRepository.findByUsernameIgnoreCase(identifier))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid member credentials");
        }

        member.setLoginCount(member.getLoginCount() + 1);
        member.setLastLoginAt(LocalDateTime.now());
        Member savedMember = memberRepository.save(member);
        AuthToken authToken = issueToken(savedMember, null, Role.MEMBER);
        return toSessionResponse(savedMember, null, authToken.getToken());
    }

    public SessionResponse registerNutritionist(NutritionistRegisterRequest request) {
        if (nutritionistRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nutritionist email already exists");
        }

        Nutritionist nutritionist = new Nutritionist();
        nutritionist.setName(request.name());
        nutritionist.setUsername(request.username());
        nutritionist.setEmail(request.email());
        nutritionist.setPasswordHash(passwordEncoder.encode(request.password()));
        nutritionist.setSpecialization(request.specialization());
        nutritionist.setLoginCount(0);
        nutritionist.setLastLoginAt(null);

        return toSessionResponse(null, nutritionistRepository.save(nutritionist), null);
    }

    public SessionResponse loginNutritionist(AuthRequest request) {
        Nutritionist nutritionist = nutritionistRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutritionist not found"));

        if (!passwordEncoder.matches(request.password(), nutritionist.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid nutritionist credentials");
        }

        nutritionist.setLoginCount(nutritionist.getLoginCount() + 1);
        nutritionist.setLastLoginAt(LocalDateTime.now());
        Nutritionist savedNutritionist = nutritionistRepository.save(nutritionist);
        AuthToken authToken = issueToken(null, savedNutritionist, Role.NUTRITIONIST);
        return toSessionResponse(null, savedNutritionist, authToken.getToken());
    }

    public void logout(String token) {
        AuthToken authToken = authTokenRepository.findByTokenAndRevokedFalse(token)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));
        authToken.setRevoked(true);
        authTokenRepository.save(authToken);
    }

    private AuthToken issueToken(Member member, Nutritionist nutritionist, Role role) {
        AuthToken authToken = new AuthToken();
        authToken.setToken(UUID.randomUUID().toString().replace("-", ""));
        authToken.setRole(role);
        authToken.setMember(member);
        authToken.setNutritionist(nutritionist);
        authToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        authToken.setRevoked(false);
        return authTokenRepository.save(authToken);
    }

    private SessionResponse toSessionResponse(Member member, Nutritionist nutritionist, String accessToken) {
        if (member != null) {
            return new SessionResponse(
                member.getId(),
                member.getName(),
                member.getUsername(),
                member.getEmail(),
                member.getRole().name(),
                null,
                member.getLoginCount(),
                accessToken
            );
        }

        return new SessionResponse(
            nutritionist.getId(),
            nutritionist.getName(),
            nutritionist.getUsername(),
            nutritionist.getEmail(),
            nutritionist.getRole().name(),
            nutritionist.getSpecialization(),
            nutritionist.getLoginCount(),
            accessToken
        );
    }
}
