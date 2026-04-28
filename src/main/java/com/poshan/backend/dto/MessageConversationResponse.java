package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record MessageConversationResponse(
    Long counterpartId,
    String counterpartName,
    String counterpartSubtitle,
    String lastMessage,
    LocalDateTime lastMessageAt,
    LocalDateTime appointmentScheduledAt,
    String appointmentDateLabel,
    String appointmentTimeLabel,
    boolean bookingActive,
    boolean chatUnlocked
) {
}
