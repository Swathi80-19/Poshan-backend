package com.poshan.backend.security;

import com.poshan.backend.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthContext {

    public static final String REQUEST_PRINCIPAL_KEY = "poshan.authPrincipal";

    public AuthPrincipal getPrincipal() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No active request");
        }

        HttpServletRequest request = attributes.getRequest();
        Object principal = request.getAttribute(REQUEST_PRINCIPAL_KEY);

        if (principal instanceof AuthPrincipal authPrincipal) {
            return authPrincipal;
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    public Long requireMemberId() {
        AuthPrincipal principal = getPrincipal();
        if (principal.role() != Role.MEMBER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Member account required");
        }
        return principal.id();
    }

    public Long requireNutritionistId() {
        AuthPrincipal principal = getPrincipal();
        if (principal.role() != Role.NUTRITIONIST) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nutritionist account required");
        }
        return principal.id();
    }
}
