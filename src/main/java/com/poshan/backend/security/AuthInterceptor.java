package com.poshan.backend.security;

import com.poshan.backend.entity.AuthToken;
import com.poshan.backend.repository.AuthTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthTokenRepository authTokenRepository;

    public AuthInterceptor(AuthTokenRepository authTokenRepository) {
        this.authTokenRepository = authTokenRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }

        String tokenValue = authorization.substring(7).trim();
        AuthToken authToken = authTokenRepository.findByTokenAndRevokedFalse(tokenValue)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));

        if (authToken.getExpiresAt() != null && authToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token expired");
        }

        Long id = authToken.getMember() != null ? authToken.getMember().getId() : authToken.getNutritionist().getId();
        String email = authToken.getMember() != null ? authToken.getMember().getEmail() : authToken.getNutritionist().getEmail();
        request.setAttribute(AuthContext.REQUEST_PRINCIPAL_KEY, new AuthPrincipal(id, authToken.getRole(), email));
        return true;
    }
}
