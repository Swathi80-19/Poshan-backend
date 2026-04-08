package com.poshan.backend.dto;

public record PaymentRequest(
    Long memberId,
    String planId,
    String planLabel,
    Integer amount,
    Integer total
) {
}
