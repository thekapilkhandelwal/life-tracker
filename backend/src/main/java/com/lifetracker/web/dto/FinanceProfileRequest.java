package com.lifetracker.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FinanceProfileRequest(
        @NotNull @DecimalMin("0") BigDecimal monthlySalary,
        @NotNull @DecimalMin("0") BigDecimal idealNeedsPercent,
        @NotNull @DecimalMin("0") BigDecimal idealWantsPercent,
        @NotNull @DecimalMin("0") BigDecimal idealInvestPercent,
        @NotNull @DecimalMin("0") BigDecimal idealSavingsPercent,
        @NotNull @DecimalMin("0") BigDecimal goalAmount,
        @NotBlank String goalLabel
) {
}
