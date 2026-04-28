package com.poshan.backend.dto;

public record NutritionistProfileResponse(
    Long nutritionistId,
    String name,
    String username,
    String email,
    String phone,
    String specialization,
    Integer experience,
    Integer age,
    String bio,
    boolean profileCompleted
) {
}
