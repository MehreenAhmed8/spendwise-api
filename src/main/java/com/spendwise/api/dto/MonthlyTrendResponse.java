package com.spendwise.api.dto;

import java.math.BigDecimal;

public class MonthlyTrendResponse {
    private String month;
    private BigDecimal income;
    private BigDecimal expense;

    public MonthlyTrendResponse(String month, BigDecimal income, BigDecimal expense) {
        this.month = month;
        this.income = income;
        this.expense = expense;
    }

    public String getMonth() {
        return month;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public BigDecimal getExpense() {
        return expense;
    }
}
