package com.lifetracker.web.dto;

import com.lifetracker.domain.CareerItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CareerItemRequest(
        @NotNull CareerItemType type,
        @NotBlank String title,
        String description,
        String status,
        Integer priority,
        LocalDate dueDate,
        Boolean completed
) {
}
