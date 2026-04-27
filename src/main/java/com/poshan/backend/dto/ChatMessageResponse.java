package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
    Long id,
    String senderRole,
    Long senderId,
    String senderName,
    String text,
    LocalDateTime sentAt
) {
}
