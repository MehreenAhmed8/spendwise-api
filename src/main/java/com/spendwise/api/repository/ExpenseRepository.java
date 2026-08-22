package com.spendwise.api.repository;

import com.spendwise.api.entity.Expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.spendwise.api.entity.ExpenseCategory;

public interface ExpenseRepository extends JpaRepository<Expense,Long>, JpaSpecificationExecutor<Expense> {
    List<Expense> findByUserId(Long userId);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.id = :userId")
    Optional<BigDecimal> sumAmountByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT SUM(e.amount) FROM Expense e
            WHERE e.user.id = :userId
              AND e.createdAt >= :start
              AND e.createdAt < :end
            """)
    Optional<BigDecimal> sumAmountByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT e.category AS category, SUM(e.amount) AS amount
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.createdAt >= :start
              AND e.createdAt < :end
            GROUP BY e.category
            ORDER BY SUM(e.amount) DESC, e.category ASC
            """)
    List<CategoryTotal> findCategoryTotalsByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT YEAR(e.createdAt) AS year, MONTH(e.createdAt) AS month, SUM(e.amount) AS amount
            FROM Expense e
            WHERE e.user.id = :userId
              AND e.createdAt >= :start
              AND e.createdAt < :end
            GROUP BY YEAR(e.createdAt), MONTH(e.createdAt)
            ORDER BY YEAR(e.createdAt), MONTH(e.createdAt)
            """)
    List<MonthlyTotal> findMonthlyTotalsByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    interface CategoryTotal {
        ExpenseCategory getCategory();

        BigDecimal getAmount();
    }

    interface MonthlyTotal {
        Integer getYear();

        Integer getMonth();

        BigDecimal getAmount();
    }
}
