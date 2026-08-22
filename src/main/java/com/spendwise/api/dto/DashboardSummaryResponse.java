package com.spendwise.api.dto;

import com.spendwise.api.entity.ExpenseCategory;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private BigDecimal savingsRate;
    private BigDecimal expenseChangePercent;
    private ExpenseCategory topCategory;
    private BigDecimal topCategoryAmount;
    private String insight;
    private List<MonthlyTrendResponse> monthlyTrend;

    public DashboardSummaryResponse(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal balance,
            BigDecimal savingsRate,
            BigDecimal expenseChangePercent,
            ExpenseCategory topCategory,
            BigDecimal topCategoryAmount,
            String insight,
            List<MonthlyTrendResponse> monthlyTrend
    ) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.balance = balance;
        this.savingsRate = savingsRate;
        this.expenseChangePercent = expenseChangePercent;
        this.topCategory = topCategory;
        this.topCategoryAmount = topCategoryAmount;
        this.insight = insight;
        this.monthlyTrend = monthlyTrend;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getSavingsRate() {
        return savingsRate;
    }

    public BigDecimal getExpenseChangePercent() {
        return expenseChangePercent;
    }

    public ExpenseCategory getTopCategory() {
        return topCategory;
    }

    public BigDecimal getTopCategoryAmount() {
        return topCategoryAmount;
    }

    public String getInsight() {
        return insight;
    }

    public List<MonthlyTrendResponse> getMonthlyTrend() {
        return monthlyTrend;
    }
}
