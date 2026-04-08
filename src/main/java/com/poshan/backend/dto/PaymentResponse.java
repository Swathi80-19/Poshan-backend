package com.poshan.backend.dto;

import java.time.LocalDateTime;

public record PaymentResponse(
    Long id,
    Long memberId,
    String planId,
    String planLabel,
    Integer amount,
    Integer total,
    String transactionId,
    String status,
    LocalDateTime paidAt
) {
}
