package com.poshan.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record PhoneOtpResendRequest(
    @NotBlank String challengeId
) {
}
