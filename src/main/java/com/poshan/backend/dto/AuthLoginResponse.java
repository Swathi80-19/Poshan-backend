package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record AuthLoginResponse(
    Long id,
    String name,
    String username,
    String email,
    String phone,
    String role,
    String specialization,
    Integer experience,
    Integer age,
    Integer loginCount,
    String accessToken,
    boolean emailVerified,
    LocalDateTime emailVerifiedAt,
    boolean profileCompleted,
    String message
) {
}
