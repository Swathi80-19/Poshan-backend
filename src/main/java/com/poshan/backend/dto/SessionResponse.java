package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record SessionResponse(
    Long id,
    String name,
    String username,
    String email,
    String phone,
    String role,
    String specialization,
    Integer experience,
    Integer loginCount,
    String accessToken,
    boolean emailVerified,
    LocalDateTime emailVerifiedAt
) {
}
