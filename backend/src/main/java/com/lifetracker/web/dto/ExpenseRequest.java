package com.lifetracker.web.dto;

import com.lifetracker.domain.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotBlank String title,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull ExpenseCategory category,
        String note,
        LocalDate spentOn
) {
}
