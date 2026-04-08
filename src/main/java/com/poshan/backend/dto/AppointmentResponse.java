package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record AppointmentResponse(
    Long id,
    Long memberId,
    String memberName,
    Long nutritionistId,
    String nutritionistName,
    String dateLabel,
    String timeLabel,
    String mode,
    String status,
    LocalDateTime scheduledAt,
    String notes
) {
}
