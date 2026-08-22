package com.spendwise.api.service;

import com.spendwise.api.auth.AuthService;
import com.spendwise.api.dto.DashboardSummaryResponse;
import com.spendwise.api.dto.MonthlyTrendResponse;
import com.spendwise.api.entity.User;
import com.spendwise.api.repository.ExpenseRepository;
import com.spendwise.api.repository.IncomeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.spendwise.api.entity.ExpenseCategory;

@Service
public class DashboardService {
    private static final BigDecimal ZERO_PERCENT = new BigDecimal("0.00");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
    private static final BigDecimal TWENTY = new BigDecimal("20");
    private static final BigDecimal NEGATIVE_TWENTY = new BigDecimal("-20");
    private static final BigDecimal THIRTY = new BigDecimal("30");

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
        BigDecimal balance = totalIncome.subtract(totalExpense);

        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
        LocalDateTime currentMonthStart = currentMonth.atStartOfDay();
        LocalDateTime nextMonthStart = currentMonth.plusMonths(1).atStartOfDay();
        LocalDateTime previousMonthStart = currentMonth.minusMonths(1).atStartOfDay();

        BigDecimal currentMonthExpense = expenseRepository
                .sumAmountByUserIdAndCreatedAtBetween(user.getId(), currentMonthStart, nextMonthStart)
                .orElse(BigDecimal.ZERO);
        BigDecimal previousMonthExpense = expenseRepository
                .sumAmountByUserIdAndCreatedAtBetween(user.getId(), previousMonthStart, currentMonthStart)
                .orElse(BigDecimal.ZERO);

        BigDecimal savingsRate = percentage(balance, totalIncome);
        BigDecimal expenseChangePercent = expenseChange(currentMonthExpense, previousMonthExpense);

        List<ExpenseRepository.CategoryTotal> categoryTotals = expenseRepository
                .findCategoryTotalsByUserIdAndCreatedAtBetween(
                        user.getId(), currentMonthStart, nextMonthStart
                );
        ExpenseCategory topCategory = categoryTotals.isEmpty()
                ? null
                : categoryTotals.get(0).getCategory();
        BigDecimal topCategoryAmount = categoryTotals.isEmpty()
                ? BigDecimal.ZERO
                : categoryTotals.get(0).getAmount();
        List<MonthlyTrendResponse> monthlyTrend = buildMonthlyTrend(
                user.getId(), YearMonth.from(currentMonth)
        );

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpense,
                balance,
                savingsRate,
                expenseChangePercent,
                topCategory,
                topCategoryAmount,
                buildInsight(totalIncome, totalExpense, savingsRate, expenseChangePercent, topCategory),
                monthlyTrend
        );
    }

    private List<MonthlyTrendResponse> buildMonthlyTrend(Long userId, YearMonth currentMonth) {
        YearMonth firstMonth = currentMonth.minusMonths(5);
        LocalDateTime start = firstMonth.atDay(1).atStartOfDay();
        LocalDateTime end = currentMonth.plusMonths(1).atDay(1).atStartOfDay();

        Map<YearMonth, BigDecimal> incomeByMonth = new HashMap<>();
        incomeRepository.findMonthlyTotalsByUserIdAndCreatedAtBetween(userId, start, end)
                .forEach(total -> incomeByMonth.put(
                        YearMonth.of(total.getYear(), total.getMonth()), total.getAmount()
                ));

        Map<YearMonth, BigDecimal> expenseByMonth = new HashMap<>();
        expenseRepository.findMonthlyTotalsByUserIdAndCreatedAtBetween(userId, start, end)
                .forEach(total -> expenseByMonth.put(
                        YearMonth.of(total.getYear(), total.getMonth()), total.getAmount()
                ));

        return IntStream.range(0, 6)
                .mapToObj(firstMonth::plusMonths)
                .map(month -> new MonthlyTrendResponse(
                        month.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        incomeByMonth.getOrDefault(month, ZERO_PERCENT),
                        expenseByMonth.getOrDefault(month, ZERO_PERCENT)
                ))
                .toList();
    }

    private BigDecimal percentage(BigDecimal amount, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) == 0) {
            return ZERO_PERCENT;
        }
        return amount.multiply(ONE_HUNDRED).divide(base, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal expenseChange(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? ZERO_PERCENT : ONE_HUNDRED;
        }
        return percentage(current.subtract(previous), previous);
    }

    private String buildInsight(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal savingsRate,
            BigDecimal expenseChangePercent,
            ExpenseCategory topCategory
    ) {
        if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return "Add income to better understand your spending health.";
        }
        if (totalExpense.compareTo(totalIncome) > 0) {
            return "Your expenses are higher than your income this month.";
        }
        if (expenseChangePercent.compareTo(TWENTY) >= 0) {
            return "Your spending increased significantly compared with last month.";
        }
        if (expenseChangePercent.compareTo(NEGATIVE_TWENTY) <= 0) {
            return "Your spending decreased significantly compared with last month.";
        }
        if (savingsRate.compareTo(THIRTY) >= 0) {
            return "You are saving a healthy portion of your income this month.";
        }
        if (topCategory != null) {
            return topCategory + " is your highest spending category this month.";
        }
        return "Your spending is currently stable.";
    }
}
