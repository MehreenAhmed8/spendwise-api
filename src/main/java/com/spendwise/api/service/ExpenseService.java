package com.spendwise.api.service;

import com.spendwise.api.auth.AuthService;
import com.spendwise.api.dto.CreateExpenseRequest;
import com.spendwise.api.dto.ExpenseResponse;
import com.spendwise.api.entity.Expense;
import com.spendwise.api.entity.User;
import com.spendwise.api.exception.ResourceNotFoundException;
import com.spendwise.api.repository.ExpenseRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final AuthService authService;


    public ExpenseService(ExpenseRepository expenseRepository, AuthService authService) {
        this.expenseRepository = expenseRepository;
        this.authService = authService;
    }

    public List<Expense> getAllExpenses() {

        User user = authService.getAuthenticatedUser();

        return expenseRepository.findByUserId(user.getId());
    }

    public ExpenseResponse createExpense(CreateExpenseRequest request) {

        User user = authService.getAuthenticatedUser();

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);

        return new ExpenseResponse(
                savedExpense.getId(),
                savedExpense.getDescription(),
                savedExpense.getAmount(),
                savedExpense.getCreatedAt(),
                savedExpense.getUpdatedAt()
        );
    }

    public void deleteExpense(long id) {

        User user = authService.getAuthenticatedUser();

        Expense expense = expenseRepository.findByIdAndUserId(
                        id,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense with id " + id + " not found"
                        )
                );

        expenseRepository.delete(expense);
    }

    public ExpenseResponse updateExpense(
            long id,
            CreateExpenseRequest request
    ) {
        User user = authService.getAuthenticatedUser();

        Expense expense = expenseRepository.findByIdAndUserId(
                        id,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense with id " + id + " not found"
                        )
                );

        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());

        Expense updatedExpense = expenseRepository.save(expense);

        return new ExpenseResponse(
                updatedExpense.getId(),
                updatedExpense.getDescription(),
                updatedExpense.getAmount(),
                updatedExpense.getCreatedAt(),
                updatedExpense.getUpdatedAt()
        );
    }

    public Expense getExpenseById(long id) {

        User user = authService.getAuthenticatedUser();

        return expenseRepository.findByIdAndUserId(
                        id,
                        user.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Expense with id " + id + " not found"
                        )
                );
    }

}
