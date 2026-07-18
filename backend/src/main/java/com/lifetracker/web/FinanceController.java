package com.lifetracker.web;

import com.lifetracker.domain.Expense;
import com.lifetracker.domain.FinanceProfile;
import com.lifetracker.security.UserPrincipal;
import com.lifetracker.service.FinanceService;
import com.lifetracker.web.dto.ExpenseRequest;
import com.lifetracker.web.dto.FinanceOverviewResponse;
import com.lifetracker.web.dto.FinanceProfileRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/overview")
    public FinanceOverviewResponse overview() {
        UserPrincipal user = AuthSupport.currentUser();
        return financeService.overview(user.getId());
    }

    @GetMapping("/profile")
    public FinanceProfile profile() {
        return financeService.getOrCreateProfile(AuthSupport.currentUser().getId());
    }

    @PutMapping("/profile")
    public FinanceProfile updateProfile(@Valid @RequestBody FinanceProfileRequest request) {
        return financeService.updateProfile(AuthSupport.currentUser().getId(), request);
    }

    @GetMapping("/expenses")
    public List<Expense> expenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return financeService.listExpenses(AuthSupport.currentUser().getId(), from, to);
    }

    @PostMapping("/expenses")
    public Expense addExpense(@Valid @RequestBody ExpenseRequest request) {
        return financeService.addExpense(AuthSupport.currentUser().getId(), request);
    }

    @DeleteMapping("/expenses/{id}")
    public void deleteExpense(@PathVariable String id) {
        financeService.deleteExpense(AuthSupport.currentUser().getId(), id);
    }
}
