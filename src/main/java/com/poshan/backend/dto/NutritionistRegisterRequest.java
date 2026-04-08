package com.poshan.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record NutritionistRegisterRequest(
    @NotBlank String name,
    @NotBlank String username,
    @Email @NotBlank String email,
    String phone,
    Integer experience,
    @NotBlank String password,
    @NotBlank String specialization
) {
}
