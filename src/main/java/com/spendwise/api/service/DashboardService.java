package com.spendwise.api.service;

import com.spendwise.api.auth.AuthService;
import com.spendwise.api.dto.DashboardSummaryResponse;
import com.spendwise.api.entity.User;
import com.spendwise.api.repository.ExpenseRepository;
import com.spendwise.api.repository.IncomeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DashboardService {
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final AuthService authService;

    public DashboardService(
            IncomeRepository incomeRepository,
            ExpenseRepository expenseRepository,
            AuthService authService
    ) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
        this.authService = authService;
    }

    public DashboardSummaryResponse getSummary() {
        User user = authService.getAuthenticatedUser();
        BigDecimal totalIncome = incomeRepository.sumAmountByUserId(user.getId())
                .orElse(BigDecimal.ZERO);
        BigDecimal totalExpense = expenseRepository.sumAmountByUserId(user.getId())
                .orElse(BigDecimal.ZERO);

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpense,
                totalIncome.subtract(totalExpense)
        );
    }
}
