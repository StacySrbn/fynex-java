package org.example.app.service.impl;

import org.example.app.dto.FinancialStatisticsResponse;
import org.example.app.dto.FinancialStatisticsResponse.CategoryAmount;
import org.example.app.dto.StatisticsFilter;
import org.example.app.entity.Transaction;
import org.example.app.entity.TransactionType;
import org.example.app.repository.TransactionRepository;
import org.example.app.service.StatisticsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionRepository transactionRepository;

    public StatisticsServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public FinancialStatisticsResponse getStatistics(Long userId, StatisticsFilter filter) {
        List<Transaction> transactions;

        if (filter.getDateFrom() != null && filter.getDateTo() != null && filter.getType() != null) {
            transactions = transactionRepository.findByUserIdAndDateBetweenAndType(userId, filter.getDateFrom(), filter.getDateTo(), filter.getType());
        } else if (filter.getDateFrom() != null && filter.getDateTo() != null) {
            transactions = transactionRepository.findByUserIdAndDateBetween(userId, filter.getDateFrom(), filter.getDateTo());
        } else if (filter.getType() != null) {
            transactions = transactionRepository.findByUserIdAndType(userId, filter.getType());
        } else {
            transactions = transactionRepository.findByUserIdOrderByDateDesc(userId);
        }

        BigDecimal totalIncome = transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryBreakdown = transactions.stream()
            .filter(t -> t.getCategory() != null)
            .collect(Collectors.groupingBy(
                t -> t.getCategory().getName(),
                Collectors.mapping(Transaction::getAmount,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ));

        List<CategoryAmount> topExpenseCategories = transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE && t.getCategory() != null)
            .collect(Collectors.groupingBy(
                t -> t.getCategory().getName(),
                Collectors.mapping(Transaction::getAmount,
                    Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
            ))
            .entrySet().stream()
            .map(e -> new CategoryAmount(e.getKey(), e.getValue()))
            .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
            .collect(Collectors.toList());

        return FinancialStatisticsResponse.builder()
            .totalIncome(totalIncome)
            .totalExpense(totalExpense)
            .balance(totalIncome.subtract(totalExpense))
            .categoryBreakdown(categoryBreakdown)
            .topExpenseCategories(topExpenseCategories)
            .build();
    }
}
