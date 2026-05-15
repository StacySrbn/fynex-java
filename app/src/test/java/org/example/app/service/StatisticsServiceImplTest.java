package org.example.app.service;

import org.example.app.dto.FinancialStatisticsResponse;
import org.example.app.dto.FinancialStatisticsResponse.CategoryAmount;
import org.example.app.dto.StatisticsFilter;
import org.example.app.entity.*;
import org.example.app.repository.TransactionRepository;
import org.example.app.service.impl.StatisticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    private Transaction createTransaction(Long id, BigDecimal amount, LocalDate date,
                                           TransactionType type, String categoryName) {
        Transaction t = new Transaction();
        t.setId(id);
        t.setAmount(amount);
        t.setDate(date);
        t.setType(type);
        t.setSource(TransactionSource.MANUAL);
        if (categoryName != null) {
            Category cat = new Category();
            cat.setId(id);
            cat.setName(categoryName);
            t.setCategory(cat);
        }
        return t;
    }

    @Test
    void getStatistics_withDateRange_shouldReturnAggregatedData() {
        Long userId = 1L;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        StatisticsFilter filter = StatisticsFilter.builder()
                .dateFrom(from).dateTo(to).build();

        Transaction income = createTransaction(1L, BigDecimal.valueOf(5000),
                LocalDate.of(2024, 6, 15), TransactionType.INCOME, "Salary");
        Transaction expense1 = createTransaction(2L, BigDecimal.valueOf(1000),
                LocalDate.of(2024, 6, 16), TransactionType.EXPENSE, "Food");
        Transaction expense2 = createTransaction(3L, BigDecimal.valueOf(500),
                LocalDate.of(2024, 6, 17), TransactionType.EXPENSE, "Transport");

        when(transactionRepository.findByUserIdAndDateBetween(userId, from, to))
                .thenReturn(Arrays.asList(income, expense1, expense2));

        FinancialStatisticsResponse result = statisticsService.getStatistics(userId, filter);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(5000), result.getTotalIncome());
        assertEquals(BigDecimal.valueOf(1500), result.getTotalExpense());
        assertEquals(BigDecimal.valueOf(3500), result.getBalance());
        assertNotNull(result.getCategoryBreakdown());
        assertNotNull(result.getTopExpenseCategories());
        verify(transactionRepository).findByUserIdAndDateBetween(userId, from, to);
    }

    @Test
    void getStatistics_withTypeFilter_shouldUseTypeAndDate() {
        Long userId = 1L;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        StatisticsFilter filter = StatisticsFilter.builder()
                .dateFrom(from).dateTo(to)
                .type(TransactionType.EXPENSE).build();

        when(transactionRepository.findByUserIdAndDateBetweenAndType(userId, from, to, TransactionType.EXPENSE))
                .thenReturn(List.of());

        FinancialStatisticsResponse result = statisticsService.getStatistics(userId, filter);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalIncome());
        assertEquals(BigDecimal.ZERO, result.getTotalExpense());
        verify(transactionRepository).findByUserIdAndDateBetweenAndType(userId, from, to, TransactionType.EXPENSE);
    }

    @Test
    void getStatistics_withOnlyTypeFilter_shouldCallTypeOnlyQuery() {
        Long userId = 1L;
        StatisticsFilter filter = StatisticsFilter.builder().type(TransactionType.INCOME).build();

        Transaction income = createTransaction(1L, BigDecimal.valueOf(3000),
                LocalDate.of(2024, 6, 15), TransactionType.INCOME, "Freelance");

        when(transactionRepository.findByUserIdAndType(userId, TransactionType.INCOME))
                .thenReturn(Arrays.asList(income));

        FinancialStatisticsResponse result = statisticsService.getStatistics(userId, filter);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(3000), result.getTotalIncome());
        assertEquals(BigDecimal.ZERO, result.getTotalExpense());
        verify(transactionRepository).findByUserIdAndType(userId, TransactionType.INCOME);
    }

    @Test
    void getStatistics_noFilters_shouldReturnAllTransactions() {
        Long userId = 1L;
        StatisticsFilter filter = StatisticsFilter.builder().build();

        Transaction income = createTransaction(1L, BigDecimal.valueOf(2000),
                LocalDate.of(2024, 6, 15), TransactionType.INCOME, "Salary");

        when(transactionRepository.findByUserIdOrderByDateDesc(userId))
                .thenReturn(Arrays.asList(income));

        FinancialStatisticsResponse result = statisticsService.getStatistics(userId, filter);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(2000), result.getTotalIncome());
        verify(transactionRepository).findByUserIdOrderByDateDesc(userId);
    }

    @Test
    void getStatistics_shouldReturnCategoryBreakdown() {
        Long userId = 1L;
        StatisticsFilter filter = StatisticsFilter.builder().build();

        Transaction food1 = createTransaction(1L, BigDecimal.valueOf(500),
                LocalDate.of(2024, 6, 15), TransactionType.EXPENSE, "Food");
        Transaction food2 = createTransaction(2L, BigDecimal.valueOf(300),
                LocalDate.of(2024, 6, 16), TransactionType.EXPENSE, "Food");
        Transaction transport = createTransaction(3L, BigDecimal.valueOf(200),
                LocalDate.of(2024, 6, 17), TransactionType.EXPENSE, "Transport");

        when(transactionRepository.findByUserIdOrderByDateDesc(userId))
                .thenReturn(Arrays.asList(food1, food2, transport));

        FinancialStatisticsResponse result = statisticsService.getStatistics(userId, filter);

        assertNotNull(result.getCategoryBreakdown());
        assertEquals(BigDecimal.valueOf(800), result.getCategoryBreakdown().get("Food"));
        assertEquals(BigDecimal.valueOf(200), result.getCategoryBreakdown().get("Transport"));
    }

    @Test
    void getStatistics_shouldReturnTopExpenseCategoriesSorted() {
        Long userId = 1L;
        StatisticsFilter filter = StatisticsFilter.builder().build();

        Transaction food = createTransaction(1L, BigDecimal.valueOf(1000),
                LocalDate.of(2024, 6, 15), TransactionType.EXPENSE, "Food");
        Transaction transport = createTransaction(2L, BigDecimal.valueOf(2000),
                LocalDate.of(2024, 6, 16), TransactionType.EXPENSE, "Transport");
        Transaction income = createTransaction(3L, BigDecimal.valueOf(5000),
                LocalDate.of(2024, 6, 17), TransactionType.INCOME, "Salary");

        when(transactionRepository.findByUserIdOrderByDateDesc(userId))
                .thenReturn(Arrays.asList(food, transport, income));

        FinancialStatisticsResponse result = statisticsService.getStatistics(userId, filter);

        List<CategoryAmount> topCategories = result.getTopExpenseCategories();
        assertNotNull(topCategories);
        assertEquals(2, topCategories.size());
        assertEquals("Transport", topCategories.get(0).getCategoryName());
        assertEquals(BigDecimal.valueOf(2000), topCategories.get(0).getAmount());
        assertEquals("Food", topCategories.get(1).getCategoryName());
        assertEquals(BigDecimal.valueOf(1000), topCategories.get(1).getAmount());
    }

    @Test
    void getStatistics_whenNoTransactions_shouldReturnEmptyState() {
        Long userId = 1L;
        StatisticsFilter filter = StatisticsFilter.builder().build();

        when(transactionRepository.findByUserIdOrderByDateDesc(userId)).thenReturn(List.of());

        FinancialStatisticsResponse result = statisticsService.getStatistics(userId, filter);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getTotalIncome());
        assertEquals(BigDecimal.ZERO, result.getTotalExpense());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertTrue(result.getCategoryBreakdown().isEmpty());
        assertTrue(result.getTopExpenseCategories().isEmpty());
    }

    @Test
    void getStatistics_withTransactionsWithoutCategory_shouldHandleNullCategory() {
        Long userId = 1L;
        StatisticsFilter filter = StatisticsFilter.builder().build();

        Transaction t = new Transaction();
        t.setId(1L);
        t.setAmount(BigDecimal.valueOf(100));
        t.setDate(LocalDate.of(2024, 6, 15));
        t.setType(TransactionType.EXPENSE);
        t.setCategory(null);

        when(transactionRepository.findByUserIdOrderByDateDesc(userId)).thenReturn(Arrays.asList(t));

        FinancialStatisticsResponse result = statisticsService.getStatistics(userId, filter);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), result.getTotalExpense());
        assertTrue(result.getCategoryBreakdown().isEmpty());
    }
}
