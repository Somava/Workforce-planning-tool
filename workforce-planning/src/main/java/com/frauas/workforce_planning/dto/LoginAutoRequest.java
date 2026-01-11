package com.frauas.workforce_planning.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginAutoRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {}
