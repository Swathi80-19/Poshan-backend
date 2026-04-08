package com.poshan.backend.dto;

public record NutritionistPatientResponse(
    Long memberId,
    String name,
    Integer age,
    String goalFocus,
    Integer sessions,
    Integer foodLogs,
    Integer activityLogs
) {
}
