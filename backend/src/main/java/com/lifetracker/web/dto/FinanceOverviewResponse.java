package com.lifetracker.web.dto;

import com.lifetracker.domain.Expense;
import com.lifetracker.domain.ExpenseCategory;
import com.lifetracker.domain.FinanceProfile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record FinanceOverviewResponse(
        FinanceProfile profile,
        List<Expense> expenses,
        Map<ExpenseCategory, BigDecimal> spentByCategory,
        BigDecimal idealNeedsAmount,
        BigDecimal idealWantsAmount,
        BigDecimal idealInvestAmount,
        BigDecimal idealSavingsAmount,
        BigDecimal totalSpent,
        BigDecimal remaining,
        BigDecimal goalProgressPercent,
        List<SpendingFlag> flags,
        List<String> advice
) {
}
