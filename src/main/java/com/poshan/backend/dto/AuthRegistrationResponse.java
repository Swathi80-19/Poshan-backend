package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record AuthRegistrationResponse(
    String message,
    String email,
    String role,
    boolean verificationRequired,
    boolean emailVerified,
    LocalDateTime emailVerifiedAt
) {
}
