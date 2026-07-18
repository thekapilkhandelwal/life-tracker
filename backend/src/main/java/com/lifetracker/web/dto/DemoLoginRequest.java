package com.lifetracker.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DemoLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String name
) {
}
