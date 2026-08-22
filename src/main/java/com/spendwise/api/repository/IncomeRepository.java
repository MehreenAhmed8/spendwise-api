package com.spendwise.api.repository;

import com.spendwise.api.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserId(Long userId);

    Optional<Income> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user.id = :userId")
    Optional<BigDecimal> sumAmountByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT YEAR(i.createdAt) AS year, MONTH(i.createdAt) AS month, SUM(i.amount) AS amount
            FROM Income i
            WHERE i.user.id = :userId
              AND i.createdAt >= :start
              AND i.createdAt < :end
            GROUP BY YEAR(i.createdAt), MONTH(i.createdAt)
            ORDER BY YEAR(i.createdAt), MONTH(i.createdAt)
            """)
    List<MonthlyTotal> findMonthlyTotalsByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    interface MonthlyTotal {
        Integer getYear();

        Integer getMonth();

        BigDecimal getAmount();
    }
}
