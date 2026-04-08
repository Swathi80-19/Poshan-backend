package com.poshan.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NutritionistRegisterRequest(
    @NotBlank String name,
    @NotBlank String username,
    @Email @NotBlank String email,
    @NotBlank String password,
    @NotBlank String specialization
) {
}
