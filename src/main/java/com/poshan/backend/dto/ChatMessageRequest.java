package com.poshan.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(@NotBlank String text) {
}
