package com.poshan.backend.dto;

public record MemberProfileRequest(
    Integer age,
    String gender,
    Double heightCm,
    Double currentWeightKg,
    Double targetWeightKg,
    String activityLevel,
    String goalFocus,
    Integer calorieGoal,
    Integer proteinGoal,
    Integer carbsGoal,
    Integer fatsGoal,
    Integer fiberGoal,
    Integer waterGoal,
    Integer stepGoal,
    Integer activeMinutesGoal,
    Double sleepGoal
) {
}
