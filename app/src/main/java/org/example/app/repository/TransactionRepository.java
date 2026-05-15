package org.example.app.repository;

import org.example.app.entity.Transaction;
import org.example.app.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByDateDesc(Long userId);

    List<Transaction> findByUserIdAndDateBetween(Long userId, LocalDate from, LocalDate to);

    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);

    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Transaction> findByUserIdAndDescriptionContainingIgnoreCase(Long userId, String keyword);

    List<Transaction> findByUserIdOrderByAmountDesc(Long userId);

    List<Transaction> findByUserIdAndDateBetweenAndType(Long userId, LocalDate from, LocalDate to, TransactionType type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = org.example.app.entity.TransactionType.INCOME")
    Optional<BigDecimal> sumIncomeByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.user.id = :userId AND t.type = org.example.app.entity.TransactionType.EXPENSE")
    Optional<BigDecimal> sumExpenseByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.category.id = :categoryId AND t.date BETWEEN :from AND :to")
    Optional<BigDecimal> sumByCategoryIdAndDateBetween(@Param("categoryId") Long categoryId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
