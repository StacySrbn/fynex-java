package org.example.app.service;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.mapper.BudgetMapper;
import org.example.app.repository.BudgetRepository;
import org.example.app.repository.CategoryRepository;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.impl.BudgetServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    @Test
    void createBudget_shouldCreateAndReturnResponse() {
        Long userId = 1L;
        CreateBudgetRequest request = CreateBudgetRequest.builder()
                .categoryId(10L)
                .limitAmount(BigDecimal.valueOf(1000))
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .build();
        User user = new User();
        user.setId(userId);
        Category category = new Category();
        category.setId(10L);
        category.setName("Food");
        Budget budget = new Budget();
        budget.setLimitAmount(BigDecimal.valueOf(1000));
        Budget saved = new Budget();
        saved.setId(1L);
        saved.setLimitAmount(BigDecimal.valueOf(1000));
        BudgetResponse response = BudgetResponse.builder()
                .id(1L)
                .limitAmount(BigDecimal.valueOf(1000))
                .categoryName("Food")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(budgetRepository.save(any(Budget.class))).thenReturn(saved);
        when(budgetMapper.toResponse(saved)).thenReturn(response);

        BudgetResponse result = budgetService.createBudget(userId, request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(1000), result.getLimitAmount());
        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(10L);
        verify(budgetRepository).save(any(Budget.class));
        verify(budgetMapper).toResponse(saved);
    }

    @Test
    void getBudgetReport_shouldReturnReportWithCalculations() {
        Long budgetId = 1L;
        Budget budget = new Budget();
        budget.setId(budgetId);
        Category category = new Category();
        category.setId(10L);
        category.setName("Food");
        budget.setCategory(category);
        budget.setLimitAmount(BigDecimal.valueOf(1000));
        budget.setStartDate(LocalDate.of(2024, 1, 1));
        budget.setEndDate(LocalDate.of(2024, 12, 31));

        BigDecimal spent = BigDecimal.valueOf(350);
        BudgetReportResponse response = BudgetReportResponse.builder()
                .budgetId(budgetId)
                .categoryName("Food")
                .limitAmount(BigDecimal.valueOf(1000))
                .spentAmount(spent)
                .remainingAmount(BigDecimal.valueOf(650))
                .usagePercentage(BigDecimal.valueOf(35.00))
                .build();

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(transactionRepository.sumByCategoryIdAndDateBetween(10L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .thenReturn(Optional.of(spent));
        when(budgetMapper.toReportResponse(budget, spent)).thenReturn(response);

        BudgetReportResponse result = budgetService.getBudgetReport(budgetId);

        assertNotNull(result);
        assertEquals(budgetId, result.getBudgetId());
        assertEquals("Food", result.getCategoryName());
        assertEquals(BigDecimal.valueOf(1000), result.getLimitAmount());
        assertEquals(BigDecimal.valueOf(350), result.getSpentAmount());
        assertEquals(BigDecimal.valueOf(650), result.getRemainingAmount());
        assertEquals(BigDecimal.valueOf(35.00), result.getUsagePercentage());
        verify(budgetRepository).findById(budgetId);
        verify(transactionRepository).sumByCategoryIdAndDateBetween(10L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
        verify(budgetMapper).toReportResponse(budget, spent);
    }

    @Test
    void getBudgetReport_whenNoSpent_shouldReturnZero() {
        Long budgetId = 1L;
        Budget budget = new Budget();
        budget.setId(budgetId);
        Category category = new Category();
        category.setId(10L);
        category.setName("Food");
        budget.setCategory(category);
        budget.setLimitAmount(BigDecimal.valueOf(1000));
        budget.setStartDate(LocalDate.of(2024, 1, 1));
        budget.setEndDate(LocalDate.of(2024, 12, 31));

        BudgetReportResponse response = BudgetReportResponse.builder()
                .budgetId(budgetId)
                .limitAmount(BigDecimal.valueOf(1000))
                .spentAmount(BigDecimal.ZERO)
                .remainingAmount(BigDecimal.valueOf(1000))
                .usagePercentage(BigDecimal.ZERO)
                .build();

        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));
        when(transactionRepository.sumByCategoryIdAndDateBetween(10L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)))
                .thenReturn(Optional.empty());
        when(budgetMapper.toReportResponse(budget, BigDecimal.ZERO)).thenReturn(response);

        BudgetReportResponse result = budgetService.getBudgetReport(budgetId);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getSpentAmount());
        verify(transactionRepository).sumByCategoryIdAndDateBetween(10L, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
    }
}
