package org.example.app.repository;

import org.example.app.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserId(Long userId);

    List<RecurringTransaction> findByActiveTrueAndStartDateLessThanEqual(LocalDate date);

    @Query("SELECT r FROM RecurringTransaction r WHERE r.active = true AND r.startDate <= :today AND (r.endDate IS NULL OR r.endDate >= :today)")
    List<RecurringTransaction> findActiveForDate(@Param("today") LocalDate today);
}
