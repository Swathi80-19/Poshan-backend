package com.poshan.backend.controller;

import com.poshan.backend.dto.AuthRequest;
import com.poshan.backend.dto.MemberRegisterRequest;
import com.poshan.backend.dto.NutritionistRegisterRequest;
import com.poshan.backend.dto.SessionResponse;
import com.poshan.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
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
    public SessionResponse registerMember(@Valid @RequestBody MemberRegisterRequest request) {
        return authService.registerMember(request);
    }

    @PostMapping("/members/login")
    public SessionResponse loginMember(@Valid @RequestBody AuthRequest request) {
        return authService.loginMember(request);
    }

    @PostMapping("/nutritionists/register")
    public SessionResponse registerNutritionist(@Valid @RequestBody NutritionistRegisterRequest request) {
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
}
