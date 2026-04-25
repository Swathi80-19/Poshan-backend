package com.poshan.backend.controller;

import com.poshan.backend.dto.AuthRequest;
import com.poshan.backend.dto.AuthRegistrationResponse;
import com.poshan.backend.dto.EmailVerificationRequest;
import com.poshan.backend.dto.EmailVerificationResponse;
import com.poshan.backend.dto.MemberRegisterRequest;
import com.poshan.backend.dto.NutritionistRegisterRequest;
import com.poshan.backend.dto.ResendVerificationRequest;
import com.poshan.backend.dto.SessionResponse;
import com.poshan.backend.dto.VerificationStatusResponse;
import com.poshan.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/members/register")
    public AuthRegistrationResponse registerMember(@Valid @RequestBody MemberRegisterRequest request) {
        return authService.registerMember(request);
    }

    @PostMapping("/members/login")
    public SessionResponse loginMember(@Valid @RequestBody AuthRequest request) {
        return authService.loginMember(request);
    }

    @PostMapping("/nutritionists/register")
    public AuthRegistrationResponse registerNutritionist(@Valid @RequestBody NutritionistRegisterRequest request) {
        return authService.registerNutritionist(request);
    }

    @PostMapping("/nutritionists/login")
    public SessionResponse loginNutritionist(@Valid @RequestBody AuthRequest request) {
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
}
