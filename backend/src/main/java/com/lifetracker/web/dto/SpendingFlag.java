package com.lifetracker.web.dto;

import java.math.BigDecimal;

public record SpendingFlag(
        String title,
        String message,
        BigDecimal actual,
        BigDecimal ideal,
        String severity
) {
}
