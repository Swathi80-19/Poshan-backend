package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record EmailVerificationResponse(
    String message,
    String email,
    String role,
    boolean verified,
    LocalDateTime verifiedAt
) {
}
