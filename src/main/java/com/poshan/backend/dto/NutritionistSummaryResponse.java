package com.poshan.backend.dto;

public record NutritionistSummaryResponse(
    Long id,
    String name,
    String username,
    String email,
    String specialization,
    Integer experience
) {
}
