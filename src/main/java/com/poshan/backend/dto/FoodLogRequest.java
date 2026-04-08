package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record FoodLogRequest(
    String dayLabel,
    String mealType,
    String foodName,
    Integer calories,
    Integer protein,
    Integer carbs,
    Integer fats,
    Integer fiber,
    LocalDateTime loggedAt
) {
}
