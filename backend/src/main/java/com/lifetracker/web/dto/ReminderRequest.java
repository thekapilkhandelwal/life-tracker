package com.lifetracker.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record ReminderRequest(
        @NotBlank String subject,
        @NotBlank String body,
        @NotNull Instant sendAt
) {
}
