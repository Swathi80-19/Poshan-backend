package com.poshan.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PhoneOtpVerifyRequest(
    @NotBlank String challengeId,
    @NotBlank String otp
) {
}
