package com.spendwise.api.service;

import com.spendwise.api.auth.AuthService;
import com.spendwise.api.dto.CreateExpenseRequest;
import com.spendwise.api.dto.ExpenseResponse;
import com.spendwise.api.dto.ExpensePageResponse;
import com.spendwise.api.entity.Expense;
import com.spendwise.api.entity.ExpenseCategory;
import com.spendwise.api.entity.User;
import com.spendwise.api.exception.ResourceNotFoundException;
import com.spendwise.api.repository.ExpenseRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final AuthService authService;


    public ExpenseService(ExpenseRepository expenseRepository, AuthService authService) {
        this.expenseRepository = expenseRepository;
        this.authService = authService;
    }

    public ExpensePageResponse getAllExpenses(
            int page,
            int size,
            ExpenseCategory category,
            LocalDate from,
            LocalDate to,
            String sortBy,
            Sort.Direction direction
    ) {
        User user = authService.getAuthenticatedUser();
        if (page < 0 || size < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page must be non-negative and size must be positive"
            );
        }
        String safeSortField = resolveSortField(sortBy);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, safeSortField)
        );

        Specification<Expense> specification = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), user.getId());

        if (category != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("category"), category));
        }

        if (from != null) {
            LocalDateTime fromDateTime = from.atStartOfDay();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDateTime));
        }

        if (to != null) {
            LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("createdAt"), toExclusive));
        }

        Page<ExpenseResponse> expenses = expenseRepository
                .findAll(specification, pageable)
                .map(this::toResponse);

        return new ExpensePageResponse(
                expenses.getContent(),
                expenses.getNumber(),
                expenses.getSize(),
                expenses.getTotalElements(),
                expenses.getTotalPages(),
                expenses.isFirst(),
                expenses.isLast()
        );
    }

    public ExpenseResponse createExpense(CreateExpenseRequest request) {

        User user = authService.getAuthenticatedUser();

        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);

        return new ExpenseResponse(
                savedExpense.getId(),
                savedExpense.getDescription(),
                savedExpense.getAmount(),
                savedExpense.getCategory(),
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
        expense.setCategory(request.getCategory());

        Expense updatedExpense = expenseRepository.save(expense);

        return new ExpenseResponse(
                updatedExpense.getId(),
                updatedExpense.getDescription(),
                updatedExpense.getAmount(),
                updatedExpense.getCategory(),
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

    private String resolveSortField(String sortBy) {
        return switch (sortBy) {
            case "createdAt", "updatedAt", "amount", "description" -> sortBy;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid sortBy field"
            );
        };
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }

}
