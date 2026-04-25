package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record VerificationStatusResponse(
    String email,
    String role,
    boolean emailVerified,
    LocalDateTime emailVerifiedAt
) {
}
