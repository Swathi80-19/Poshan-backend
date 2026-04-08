package com.poshan.backend.dto;

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
    String accessToken
) {
}
