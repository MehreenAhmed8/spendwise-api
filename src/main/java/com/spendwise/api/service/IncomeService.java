package com.spendwise.api.service;

import com.spendwise.api.auth.AuthService;
import com.spendwise.api.dto.CreateIncomeRequest;
import com.spendwise.api.dto.IncomeResponse;
import com.spendwise.api.entity.Income;
import com.spendwise.api.entity.User;
import com.spendwise.api.exception.ResourceNotFoundException;
import com.spendwise.api.repository.IncomeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncomeService {
    private final IncomeRepository incomeRepository;
    private final AuthService authService;

    public IncomeService(IncomeRepository incomeRepository, AuthService authService) {
        this.incomeRepository = incomeRepository;
        this.authService = authService;
    }

    public IncomeResponse createIncome(CreateIncomeRequest request) {
        User user = authService.getAuthenticatedUser();

        Income income = new Income();
        income.setDescription(request.getDescription());
        income.setAmount(request.getAmount());
        income.setUser(user);

        return toResponse(incomeRepository.save(income));
    }

    public List<IncomeResponse> getAllIncomes() {
        User user = authService.getAuthenticatedUser();

        return incomeRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public IncomeResponse getIncomeById(long id) {
        User user = authService.getAuthenticatedUser();

        return toResponse(findOwnedIncome(id, user.getId()));
    }

    public IncomeResponse updateIncome(long id, CreateIncomeRequest request) {
        User user = authService.getAuthenticatedUser();
        Income income = findOwnedIncome(id, user.getId());

        income.setDescription(request.getDescription());
        income.setAmount(request.getAmount());

        return toResponse(incomeRepository.save(income));
    }

    public void deleteIncome(long id) {
        User user = authService.getAuthenticatedUser();
        Income income = findOwnedIncome(id, user.getId());

        incomeRepository.delete(income);
    }

    private Income findOwnedIncome(long id, Long userId) {
        return incomeRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Income with id " + id + " not found"
                        )
                );
    }

    private IncomeResponse toResponse(Income income) {
        return new IncomeResponse(
                income.getId(),
                income.getDescription(),
                income.getAmount(),
                income.getCreatedAt(),
                income.getUpdatedAt()
        );
    }
}
