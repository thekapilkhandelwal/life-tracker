package com.lifetracker.service;

import com.lifetracker.domain.Expense;
import com.lifetracker.domain.ExpenseCategory;
import com.lifetracker.domain.FinanceProfile;
import com.lifetracker.repository.ExpenseRepository;
import com.lifetracker.repository.FinanceProfileRepository;
import com.lifetracker.web.NotFoundException;
import com.lifetracker.web.dto.ExpenseRequest;
import com.lifetracker.web.dto.FinanceOverviewResponse;
import com.lifetracker.web.dto.FinanceProfileRequest;
import com.lifetracker.web.dto.SpendingFlag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final FinanceProfileRepository financeProfileRepository;
    private final ExpenseRepository expenseRepository;

    public FinanceProfile getOrCreateProfile(String userId) {
        return financeProfileRepository.findByUserId(userId)
                .orElseGet(() -> financeProfileRepository.save(FinanceProfile.builder()
                        .userId(userId)
                        .monthlySalary(BigDecimal.ZERO)
                        .idealNeedsPercent(BigDecimal.valueOf(50))
                        .idealWantsPercent(BigDecimal.valueOf(30))
                        .idealInvestPercent(BigDecimal.valueOf(15))
                        .idealSavingsPercent(BigDecimal.valueOf(5))
                        .goalAmount(BigDecimal.ZERO)
                        .goalLabel("Emergency / Freedom Fund")
                        .updatedAt(Instant.now())
                        .build()));
    }

    public FinanceProfile updateProfile(String userId, FinanceProfileRequest request) {
        FinanceProfile profile = getOrCreateProfile(userId);
        profile.setMonthlySalary(request.monthlySalary());
        profile.setIdealNeedsPercent(request.idealNeedsPercent());
        profile.setIdealWantsPercent(request.idealWantsPercent());
        profile.setIdealInvestPercent(request.idealInvestPercent());
        profile.setIdealSavingsPercent(request.idealSavingsPercent());
        profile.setGoalAmount(request.goalAmount());
        profile.setGoalLabel(request.goalLabel());
        profile.setUpdatedAt(Instant.now());
        return financeProfileRepository.save(profile);
    }

    public Expense addExpense(String userId, ExpenseRequest request) {
        Expense expense = Expense.builder()
                .userId(userId)
                .title(request.title())
                .amount(request.amount())
                .category(request.category())
                .note(request.note())
                .spentOn(request.spentOn() == null ? LocalDate.now() : request.spentOn())
                .createdAt(Instant.now())
                .build();
        return expenseRepository.save(expense);
    }

    public List<Expense> listExpenses(String userId, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return expenseRepository.findByUserIdAndSpentOnBetweenOrderBySpentOnDesc(userId, from, to);
        }
        return expenseRepository.findByUserIdOrderBySpentOnDesc(userId);
    }

    public void deleteExpense(String userId, String expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(item -> userId.equals(item.getUserId()))
                .orElseThrow(() -> new NotFoundException("Expense not found"));
        expenseRepository.delete(expense);
    }

    public FinanceOverviewResponse overview(String userId) {
        FinanceProfile profile = getOrCreateProfile(userId);
        YearMonth month = YearMonth.now();
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<Expense> expenses = expenseRepository.findByUserIdAndSpentOnBetweenOrderBySpentOnDesc(userId, from, to);

        Map<ExpenseCategory, BigDecimal> spentByCategory = new EnumMap<>(ExpenseCategory.class);
        for (ExpenseCategory category : ExpenseCategory.values()) {
            spentByCategory.put(category, BigDecimal.ZERO);
        }
        BigDecimal totalSpent = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            BigDecimal amount = expense.getAmount() == null ? BigDecimal.ZERO : expense.getAmount();
            totalSpent = totalSpent.add(amount);
            spentByCategory.merge(expense.getCategory(), amount, BigDecimal::add);
        }

        BigDecimal salary = nullSafe(profile.getMonthlySalary());
        BigDecimal idealNeeds = percentOf(salary, profile.getIdealNeedsPercent());
        BigDecimal idealWants = percentOf(salary, profile.getIdealWantsPercent());
        BigDecimal idealInvest = percentOf(salary, profile.getIdealInvestPercent());
        BigDecimal idealSavings = percentOf(salary, profile.getIdealSavingsPercent());

        List<SpendingFlag> flags = new ArrayList<>();
        flagIfOver(flags, "Needs", spentByCategory.get(ExpenseCategory.NEEDS), idealNeeds, "Keep essentials closer to ~50% of income.");
        flagIfOver(flags, "Wants", spentByCategory.get(ExpenseCategory.WANTS), idealWants, "Discretionary spend is above the recommended band.");
        if (spentByCategory.get(ExpenseCategory.INVESTMENT).compareTo(idealInvest) < 0 && salary.compareTo(BigDecimal.ZERO) > 0) {
            flags.add(new SpendingFlag(
                    "Investment shortfall",
                    "You invested less than your target this month.",
                    spentByCategory.get(ExpenseCategory.INVESTMENT),
                    idealInvest,
                    "WARNING"
            ));
        }
        if (spentByCategory.get(ExpenseCategory.SAVINGS).compareTo(idealSavings) < 0 && salary.compareTo(BigDecimal.ZERO) > 0) {
            flags.add(new SpendingFlag(
                    "Savings shortfall",
                    "Savings are below your planned allocation.",
                    spentByCategory.get(ExpenseCategory.SAVINGS),
                    idealSavings,
                    "WARNING"
            ));
        }

        BigDecimal remaining = salary.subtract(totalSpent);
        BigDecimal goalAmount = nullSafe(profile.getGoalAmount());
        BigDecimal progressPercent = goalAmount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : spentByCategory.get(ExpenseCategory.SAVINGS)
                .add(spentByCategory.get(ExpenseCategory.INVESTMENT))
                .multiply(HUNDRED)
                .divide(goalAmount, 1, RoundingMode.HALF_UP);

        List<String> advice = buildAdvice(salary, spentByCategory, remaining, flags);

        return new FinanceOverviewResponse(
                profile,
                expenses,
                spentByCategory,
                idealNeeds,
                idealWants,
                idealInvest,
                idealSavings,
                totalSpent,
                remaining,
                progressPercent.min(HUNDRED),
                flags,
                advice
        );
    }

    private void flagIfOver(List<SpendingFlag> flags, String label, BigDecimal actual, BigDecimal ideal, String tip) {
        if (ideal.compareTo(BigDecimal.ZERO) > 0 && actual.compareTo(ideal) > 0) {
            flags.add(new SpendingFlag(
                    label + " overspend",
                    tip,
                    actual,
                    ideal,
                    "ALERT"
            ));
        }
    }

    private List<String> buildAdvice(
            BigDecimal salary,
            Map<ExpenseCategory, BigDecimal> spent,
            BigDecimal remaining,
            List<SpendingFlag> flags
    ) {
        List<String> advice = new ArrayList<>();
        advice.add("Baseline rule used: 50% needs / 30% wants / 15% invest / 5% cash savings (editable in profile).");
        if (salary.compareTo(BigDecimal.ZERO) <= 0) {
            advice.add("Set your monthly salary to unlock personalized targets and overspend flags.");
            return advice;
        }
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            advice.add("You are spending more than you earn this month. Pause non-essential wants until you are net positive.");
        } else {
            advice.add("Pay yourself first: move investment + savings allocations as soon as salary arrives.");
        }
        if (flags.stream().anyMatch(flag -> "ALERT".equals(flag.severity()))) {
            advice.add("Cut the flagged want/need categories first — that is where leakage is highest.");
        }
        BigDecimal invest = spent.get(ExpenseCategory.INVESTMENT);
        if (invest.compareTo(percentOf(salary, BigDecimal.valueOf(15))) < 0) {
            advice.add("Aim for at least 15% of income into long-term investments (index funds / SIPs).");
        }
        advice.add("Keep an emergency fund of 3–6 months of needs before aggressive investing.");
        return advice;
    }

    private BigDecimal percentOf(BigDecimal base, BigDecimal percent) {
        return nullSafe(base)
                .multiply(nullSafe(percent))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
