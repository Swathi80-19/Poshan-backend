package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record ActivityLogResponse(
    Long id,
    String dayLabel,
    Integer steps,
    Integer activeMinutes,
    Integer water,
    Double sleepHours,
    Integer sleepQuality,
    Double weight,
    String mood,
    String notes,
    LocalDateTime loggedAt
) {
}
