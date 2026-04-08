package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record AppointmentRequest(
    Long memberId,
    Long nutritionistId,
    String dateLabel,
    String timeLabel,
    String mode,
    LocalDateTime scheduledAt,
    String notes
) {
}
