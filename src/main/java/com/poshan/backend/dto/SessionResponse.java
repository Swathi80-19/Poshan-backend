package com.poshan.backend.dto;

public record SessionResponse(
    Long id,
    String name,
    String username,
    String email,
    String role,
    String specialization,
    Integer loginCount,
    String accessToken
) {
}
