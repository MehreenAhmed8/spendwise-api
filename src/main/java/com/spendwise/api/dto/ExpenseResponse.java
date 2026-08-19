package com.spendwise.api.dto;

import java.time.LocalDateTime;

public class ExpenseResponse {

    private Long id;
    private String description;
    private double amount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ExpenseResponse(
            Long id,
            String description,
            double amount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
