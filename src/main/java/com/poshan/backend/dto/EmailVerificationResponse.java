package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record EmailVerificationResponse(
    String message,
    String email,
    String role,
    boolean verified,
    LocalDateTime verifiedAt,
    Long id,
    String name,
    String username,
    String phone,
    String specialization,
    Integer experience,
    Integer age,
    Integer loginCount,
    String accessToken,
    boolean profileCompleted
) {
}
