package com.poshan.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MessageThreadResponse(
    Long counterpartId,
    String counterpartName,
    String counterpartSubtitle,
    LocalDateTime appointmentScheduledAt,
    String appointmentDateLabel,
    String appointmentTimeLabel,
    boolean active,
    List<ChatMessageResponse> messages
) {
}
