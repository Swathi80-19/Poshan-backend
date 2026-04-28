package com.poshan.backend.controller;

import com.poshan.backend.dto.AuthRequest;
import com.poshan.backend.dto.AuthLoginResponse;
import com.poshan.backend.dto.AuthRegistrationResponse;
import com.poshan.backend.dto.EmailVerificationRequest;
import com.poshan.backend.dto.EmailVerificationResponse;
import com.poshan.backend.dto.MemberRegisterRequest;
import com.poshan.backend.dto.NutritionistRegisterRequest;
import com.poshan.backend.dto.ResendVerificationRequest;
import com.poshan.backend.dto.VerificationStatusResponse;
import com.poshan.backend.service.AuthService;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final String frontendBaseUrl;

    public AuthController(
        AuthService authService,
        @org.springframework.beans.factory.annotation.Value("${app.frontend-base-url:}") String frontendBaseUrl
    ) {
        this.authService = authService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @PostMapping("/members/register")
    public AuthRegistrationResponse registerMember(@Valid @RequestBody MemberRegisterRequest request) {
        return authService.registerMember(request);
    }

    @PostMapping("/members/login")
    public AuthLoginResponse loginMember(@Valid @RequestBody AuthRequest request) {
        return authService.loginMember(request);
    }

    @PostMapping("/nutritionists/register")
    public AuthRegistrationResponse registerNutritionist(@Valid @RequestBody NutritionistRegisterRequest request) {
        return authService.registerNutritionist(request);
    }

    @PostMapping("/nutritionists/login")
    public AuthLoginResponse loginNutritionist(@Valid @RequestBody AuthRequest request) {
        return authService.loginNutritionist(request);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        String token = authorization.replace("Bearer ", "").trim();
        authService.logout(token);
    }

    @PostMapping("/verify-email")
    public EmailVerificationResponse verifyEmail(@Valid @RequestBody EmailVerificationRequest request) {
        return authService.verifyEmail(request);
    }

    @GetMapping("/verify-email-link")
    public ResponseEntity<Void> verifyEmailLink(@RequestParam String token) {
        String configuredFrontendBaseUrl = requirePublicFrontendBaseUrl();

        try {
            EmailVerificationResponse response = authService.verifyEmail(new EmailVerificationRequest(token));
            String redirectUrl = configuredFrontendBaseUrl
                + "/verify-email?verified=1"
                + "&email=" + encode(response.email())
                + "&role=" + encode(response.role())
                + "&message=" + encode(response.message());
            return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
        } catch (ResponseStatusException exception) {
            String redirectUrl = configuredFrontendBaseUrl
                + "/verify-email?error=" + encode(exception.getReason());
            return ResponseEntity.status(302).location(URI.create(redirectUrl)).build();
        }
    }

    @PostMapping("/resend-verification")
    public AuthRegistrationResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        return authService.resendVerification(request);
    }

    @GetMapping("/verification-status")
    public VerificationStatusResponse getVerificationStatus(
        @RequestParam String email,
        @RequestParam String role
    ) {
        return authService.getVerificationStatus(email, role);
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    private String requirePublicFrontendBaseUrl() {
        String normalized = frontendBaseUrl == null ? "" : frontendBaseUrl.trim().replaceAll("/+$", "");

        if (!StringUtils.hasText(normalized) || isLocalUrl(normalized)) {
            throw new ResponseStatusException(
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                "FRONTEND_BASE_URL must be set to your deployed app URL before email verification links can redirect users."
            );
        }

        return normalized;
    }

    private boolean isLocalUrl(String value) {
        String normalized = value.toLowerCase();
        return normalized.contains("localhost") || normalized.contains("127.0.0.1");
    }
}
