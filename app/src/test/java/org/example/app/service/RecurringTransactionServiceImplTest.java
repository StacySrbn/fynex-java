package org.example.app.service;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.RecurringTransactionMapper;
import org.example.app.repository.CategoryRepository;
import org.example.app.repository.RecurringTransactionRepository;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.impl.RecurringTransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceImplTest {

    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecurringTransactionMapper recurringTransactionMapper;

    @InjectMocks
    private RecurringTransactionServiceImpl recurringTransactionService;

    @Test
    void createRecurringTransaction_shouldCreateAndReturnResponse() {
        Long userId = 1L;
        Long categoryId = 10L;
        CreateRecurringTransactionRequest request = CreateRecurringTransactionRequest.builder()
                .title("Netflix")
                .amount(BigDecimal.valueOf(15.99))
                .startDate(LocalDate.of(2024, 1, 1))
                .frequency(Frequency.MONTHLY)
                .categoryId(categoryId)
                .build();

        User user = new User();
        user.setId(userId);
        Category category = new Category();
        category.setId(categoryId);
        RecurringTransaction rt = new RecurringTransaction();
        rt.setId(100L);
        rt.setTitle("Netflix");
        rt.setAmount(BigDecimal.valueOf(15.99));
        rt.setFrequency(Frequency.MONTHLY);
        rt.setActive(true);
        RecurringTransaction saved = new RecurringTransaction();
        saved.setId(100L);
        RecurringTransactionResponse response = RecurringTransactionResponse.builder()
                .id(100L)
                .title("Netflix")
                .amount(BigDecimal.valueOf(15.99))
                .frequency(Frequency.MONTHLY)
                .active(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(recurringTransactionRepository.save(any(RecurringTransaction.class))).thenReturn(saved);
        when(recurringTransactionMapper.toResponse(saved)).thenReturn(response);

        RecurringTransactionResponse result = recurringTransactionService.createRecurringTransaction(userId, request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Netflix", result.getTitle());
        assertEquals(Frequency.MONTHLY, result.getFrequency());
        assertTrue(result.isActive());
        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(categoryId);
        verify(recurringTransactionRepository).save(any(RecurringTransaction.class));
        verify(recurringTransactionMapper).toResponse(saved);
    }

    @Test
    void createRecurringTransaction_withEndDateAndRepeatCount_shouldSaveOptionals() {
        Long userId = 1L;
        Long categoryId = 10L;
        CreateRecurringTransactionRequest request = CreateRecurringTransactionRequest.builder()
                .title("Gym")
                .amount(BigDecimal.valueOf(50))
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .repeatCount(52)
                .frequency(Frequency.WEEKLY)
                .categoryId(categoryId)
                .build();

        User user = new User();
        user.setId(userId);
        Category category = new Category();
        category.setId(categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        ArgumentCaptor<RecurringTransaction> captor = ArgumentCaptor.forClass(RecurringTransaction.class);
        when(recurringTransactionRepository.save(captor.capture())).thenAnswer(i -> i.getArgument(0));

        recurringTransactionService.createRecurringTransaction(userId, request);

        RecurringTransaction captured = captor.getValue();
        assertEquals("Gym", captured.getTitle());
        assertEquals(LocalDate.of(2024, 12, 31), captured.getEndDate());
        assertEquals(52, captured.getRepeatCount());
        assertTrue(captured.isActive());
    }

    @Test
    void getUserRecurring_shouldReturnList() {
        Long userId = 1L;
        RecurringTransaction rt = new RecurringTransaction();
        rt.setId(1L);
        RecurringTransactionResponse response = RecurringTransactionResponse.builder().id(1L).build();

        when(recurringTransactionRepository.findByUserId(userId)).thenReturn(Arrays.asList(rt));
        when(recurringTransactionMapper.toResponse(rt)).thenReturn(response);

        List<RecurringTransactionResponse> result = recurringTransactionService.getUserRecurring(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(recurringTransactionRepository).findByUserId(userId);
    }

    @Test
    void getRecurringById_shouldReturnResponse() {
        Long id = 1L;
        RecurringTransaction rt = new RecurringTransaction();
        rt.setId(id);
        RecurringTransactionResponse response = RecurringTransactionResponse.builder().id(id).build();

        when(recurringTransactionRepository.findById(id)).thenReturn(Optional.of(rt));
        when(recurringTransactionMapper.toResponse(rt)).thenReturn(response);

        RecurringTransactionResponse result = recurringTransactionService.getRecurringById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(recurringTransactionRepository).findById(id);
    }

    @Test
    void getRecurringById_shouldThrowWhenNotFound() {
        Long id = 999L;
        when(recurringTransactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recurringTransactionService.getRecurringById(id));
        verify(recurringTransactionRepository).findById(id);
    }

    @Test
    void deactivateRecurring_shouldSetActiveFalse() {
        Long id = 1L;
        RecurringTransaction rt = new RecurringTransaction();
        rt.setId(id);
        rt.setActive(true);

        when(recurringTransactionRepository.findById(id)).thenReturn(Optional.of(rt));

        recurringTransactionService.deactivateRecurring(id);

        assertFalse(rt.isActive());
        verify(recurringTransactionRepository).save(rt);
    }

    @Test
    void deactivateRecurring_shouldThrowWhenNotFound() {
        Long id = 999L;
        when(recurringTransactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> recurringTransactionService.deactivateRecurring(id));
        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    void generateDueTransactions_withDailyFrequency_shouldGenerateTransaction() {
        LocalDate today = LocalDate.of(2025, 6, 15);
        RecurringTransaction rt = createRecurringWithFrequency(
                Frequency.DAILY, LocalDate.of(2025, 6, 10), null);

        when(recurringTransactionRepository.findActiveForDate(today)).thenReturn(Arrays.asList(rt));

        recurringTransactionService.generateDueTransactions();

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals(BigDecimal.valueOf(100), saved.getAmount());
        assertEquals(today, saved.getDate());
        assertEquals(TransactionType.EXPENSE, saved.getType());
        assertEquals(TransactionSource.RECURRING, saved.getSource());
    }

    @Test
    void generateDueTransactions_withWeeklyFrequency_shouldGenerateOnSameWeekday() {
        LocalDate today = LocalDate.of(2025, 6, 19); // Thursday
        RecurringTransaction rt = createRecurringWithFrequency(
                Frequency.WEEKLY, LocalDate.of(2025, 6, 12), null); // Thursday

        when(recurringTransactionRepository.findActiveForDate(today)).thenReturn(Arrays.asList(rt));

        recurringTransactionService.generateDueTransactions();

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void generateDueTransactions_withWeeklyFrequency_shouldNotGenerateOnWrongDay() {
        LocalDate today = LocalDate.of(2025, 6, 18); // Wednesday
        RecurringTransaction rt = createRecurringWithFrequency(
                Frequency.WEEKLY, LocalDate.of(2025, 6, 12), null); // Thursday

        when(recurringTransactionRepository.findActiveForDate(today)).thenReturn(Arrays.asList(rt));

        recurringTransactionService.generateDueTransactions();

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void generateDueTransactions_withMonthlyFrequency_shouldGenerateOnSameDay() {
        LocalDate today = LocalDate.of(2025, 7, 15);
        RecurringTransaction rt = createRecurringWithFrequency(
                Frequency.MONTHLY, LocalDate.of(2025, 6, 15), null);

        when(recurringTransactionRepository.findActiveForDate(today)).thenReturn(Arrays.asList(rt));

        recurringTransactionService.generateDueTransactions();

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void generateDueTransactions_withMonthlyFrequency_shouldNotGenerateOnWrongDay() {
        LocalDate today = LocalDate.of(2025, 7, 16);
        RecurringTransaction rt = createRecurringWithFrequency(
                Frequency.MONTHLY, LocalDate.of(2025, 6, 15), null);

        when(recurringTransactionRepository.findActiveForDate(today)).thenReturn(Arrays.asList(rt));

        recurringTransactionService.generateDueTransactions();

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void generateDueTransactions_withYearlyFrequency_shouldGenerateOnSameDate() {
        LocalDate today = LocalDate.of(2026, 6, 15);
        RecurringTransaction rt = createRecurringWithFrequency(
                Frequency.YEARLY, LocalDate.of(2025, 6, 15), null);

        when(recurringTransactionRepository.findActiveForDate(today)).thenReturn(Arrays.asList(rt));

        recurringTransactionService.generateDueTransactions();

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void generateDueTransactions_withYearlyFrequency_shouldNotGenerateOnWrongDate() {
        LocalDate today = LocalDate.of(2026, 6, 16);
        RecurringTransaction rt = createRecurringWithFrequency(
                Frequency.YEARLY, LocalDate.of(2025, 6, 15), null);

        when(recurringTransactionRepository.findActiveForDate(today)).thenReturn(Arrays.asList(rt));

        recurringTransactionService.generateDueTransactions();

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void generateDueTransactions_whenEndDatePassed_shouldNotGenerate() {
        LocalDate today = LocalDate.of(2025, 7, 1);
        RecurringTransaction rt = createRecurringWithFrequency(
                Frequency.DAILY, LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 15));

        when(recurringTransactionRepository.findActiveForDate(today)).thenReturn(Arrays.asList(rt));

        recurringTransactionService.generateDueTransactions();

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void generateDueTransactions_whenNoDueTransactions_shouldNotSave() {
        LocalDate today = LocalDate.of(2025, 6, 15);
        when(recurringTransactionRepository.findActiveForDate(today)).thenReturn(List.of());

        recurringTransactionService.generateDueTransactions();

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    private RecurringTransaction createRecurringWithFrequency(Frequency freq, LocalDate start, LocalDate end) {
        RecurringTransaction rt = new RecurringTransaction();
        rt.setId(1L);
        rt.setTitle("Test Payment");
        rt.setAmount(BigDecimal.valueOf(100));
        rt.setStartDate(start);
        rt.setEndDate(end);
        rt.setFrequency(freq);
        rt.setActive(true);
        User user = new User();
        user.setId(1L);
        rt.setUser(user);
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("Test");
        rt.setCategory(cat);
        return rt;
    }
}
