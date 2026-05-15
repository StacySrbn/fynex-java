package org.example.app.service;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.TransactionMapper;
import org.example.app.repository.CategoryRepository;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void createTransaction_shouldCreateAndReturnResponse() {
        Long userId = 1L;
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .date(LocalDate.now())
                .description("test")
                .type(TransactionType.INCOME)
                .categoryId(10L)
                .build();
        User user = new User();
        user.setId(userId);
        Category category = new Category();
        category.setId(10L);
        Transaction transaction = new Transaction();
        transaction.setId(100L);
        Transaction saved = new Transaction();
        saved.setId(100L);
        TransactionResponse response = TransactionResponse.builder()
                .id(100L)
                .amount(BigDecimal.valueOf(100))
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(transactionMapper.toEntity(request, user, category)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(saved);
        when(transactionMapper.toResponse(saved)).thenReturn(response);

        TransactionResponse result = transactionService.createTransaction(userId, request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(10L);
        verify(transactionMapper).toEntity(request, user, category);
        verify(transactionRepository).save(transaction);
        verify(transactionMapper).toResponse(saved);
    }

    @Test
    void updateTransaction_shouldUpdateAndReturnResponse() {
        Long id = 1L;
        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(200))
                .description("updated")
                .build();
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setAmount(BigDecimal.valueOf(100));
        Transaction saved = new Transaction();
        saved.setId(id);
        saved.setAmount(BigDecimal.valueOf(200));
        TransactionResponse response = TransactionResponse.builder()
                .id(id)
                .amount(BigDecimal.valueOf(200))
                .build();

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(saved);
        when(transactionMapper.toResponse(saved)).thenReturn(response);

        TransactionResponse result = transactionService.updateTransaction(id, request);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(200), result.getAmount());
        verify(transactionRepository).findById(id);
        verify(transactionRepository).save(transaction);
        verify(transactionMapper).toResponse(saved);
    }

    @Test
    void deleteTransaction_shouldDelete() {
        Long id = 1L;
        when(transactionRepository.existsById(id)).thenReturn(true);

        transactionService.deleteTransaction(id);

        verify(transactionRepository).existsById(id);
        verify(transactionRepository).deleteById(id);
    }

    @Test
    void deleteTransaction_shouldThrowWhenNotFound() {
        Long id = 1L;
        when(transactionRepository.existsById(id)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(id));

        verify(transactionRepository).existsById(id);
        verify(transactionRepository, never()).deleteById(any());
    }

    @Test
    void getTransactionById_shouldReturnResponse() {
        Long id = 1L;
        Transaction transaction = new Transaction();
        transaction.setId(id);
        TransactionResponse response = TransactionResponse.builder()
                .id(id)
                .build();

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toResponse(transaction)).thenReturn(response);

        TransactionResponse result = transactionService.getTransactionById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(transactionRepository).findById(id);
        verify(transactionMapper).toResponse(transaction);
    }

    @Test
    void getUserTransactions_shouldReturnPagedResponse() {
        Long userId = 1L;
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setDate(LocalDate.now());
        Transaction t2 = new Transaction();
        t2.setId(2L);
        t2.setAmount(BigDecimal.valueOf(200));
        t2.setDate(LocalDate.now().minusDays(1));
        List<Transaction> transactions = new ArrayList<>(Arrays.asList(t1, t2));

        when(transactionRepository.findByUserIdOrderByDateDesc(userId)).thenReturn(transactions);
        when(transactionMapper.toResponse(t1)).thenReturn(TransactionResponse.builder().id(1L).amount(BigDecimal.valueOf(100)).build());
        when(transactionMapper.toResponse(t2)).thenReturn(TransactionResponse.builder().id(2L).amount(BigDecimal.valueOf(200)).build());

        TransactionPageResponse result = transactionService.getUserTransactions(userId, null, null, null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getTransactions().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        verify(transactionRepository).findByUserIdOrderByDateDesc(userId);
    }

    @Test
    void getUserTransactions_withTypeFilter_shouldReturnFiltered() {
        Long userId = 1L;
        Transaction t = new Transaction();
        t.setId(1L);
        t.setAmount(BigDecimal.valueOf(100));
        t.setDate(LocalDate.now());
        t.setType(TransactionType.EXPENSE);
        List<Transaction> transactions = new ArrayList<>(Arrays.asList(t));

        when(transactionRepository.findByUserIdAndType(userId, TransactionType.EXPENSE)).thenReturn(transactions);
        when(transactionMapper.toResponse(t)).thenReturn(TransactionResponse.builder().id(1L).amount(BigDecimal.valueOf(100)).build());

        TransactionPageResponse result = transactionService.getUserTransactions(userId, "EXPENSE", null, null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTransactions().size());
        verify(transactionRepository).findByUserIdAndType(userId, TransactionType.EXPENSE);
    }

    @Test
    void getUserTransactions_withDateRange_shouldReturnFiltered() {
        Long userId = 1L;
        LocalDate from = LocalDate.of(2024, 1, 1);
        LocalDate to = LocalDate.of(2024, 12, 31);
        Transaction t = new Transaction();
        t.setId(1L);
        t.setAmount(BigDecimal.valueOf(50));
        t.setDate(LocalDate.of(2024, 6, 15));
        List<Transaction> transactions = new ArrayList<>(Arrays.asList(t));

        when(transactionRepository.findByUserIdAndDateBetween(userId, from, to)).thenReturn(transactions);
        when(transactionMapper.toResponse(t)).thenReturn(TransactionResponse.builder().id(1L).amount(BigDecimal.valueOf(50)).build());

        TransactionPageResponse result = transactionService.getUserTransactions(userId, null, null, from, to, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTransactions().size());
        verify(transactionRepository).findByUserIdAndDateBetween(userId, from, to);
    }

    @Test
    void getUserTransactions_withCategoryFilter_shouldReturnFiltered() {
        Long userId = 1L;
        Long categoryId = 5L;
        Transaction t = new Transaction();
        t.setId(1L);
        t.setAmount(BigDecimal.valueOf(75));
        t.setDate(LocalDate.now());
        List<Transaction> transactions = new ArrayList<>(Arrays.asList(t));

        when(transactionRepository.findByUserIdAndCategoryId(userId, categoryId)).thenReturn(transactions);
        when(transactionMapper.toResponse(t)).thenReturn(TransactionResponse.builder().id(1L).amount(BigDecimal.valueOf(75)).build());

        TransactionPageResponse result = transactionService.getUserTransactions(userId, null, categoryId, null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTransactions().size());
        verify(transactionRepository).findByUserIdAndCategoryId(userId, categoryId);
    }

    @Test
    void getUserTransactions_withSearch_shouldFilterByDescription() {
        Long userId = 1L;
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setAmount(BigDecimal.valueOf(100));
        t1.setDate(LocalDate.now());
        t1.setDescription("Groceries");
        Transaction t2 = new Transaction();
        t2.setId(2L);
        t2.setAmount(BigDecimal.valueOf(200));
        t2.setDate(LocalDate.now());
        t2.setDescription("Salary");
        List<Transaction> transactions = new ArrayList<>(Arrays.asList(t1, t2));

        when(transactionRepository.findByUserIdOrderByDateDesc(userId)).thenReturn(transactions);
        when(transactionMapper.toResponse(t1)).thenReturn(TransactionResponse.builder().id(1L).amount(BigDecimal.valueOf(100)).description("Groceries").build());

        TransactionPageResponse result = transactionService.getUserTransactions(userId, null, null, null, null, "groceries", null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTransactions().size());
        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findByUserIdOrderByDateDesc(userId);
    }

    @Test
    void getUserTransactions_withSortByAmount_shouldSortDescending() {
        Long userId = 1L;
        Transaction t1 = new Transaction();
        t1.setId(1L);
        t1.setAmount(BigDecimal.valueOf(50));
        t1.setDate(LocalDate.now());
        Transaction t2 = new Transaction();
        t2.setId(2L);
        t2.setAmount(BigDecimal.valueOf(200));
        t2.setDate(LocalDate.now());
        List<Transaction> transactions = new ArrayList<>(Arrays.asList(t1, t2));

        when(transactionRepository.findByUserIdOrderByDateDesc(userId)).thenReturn(transactions);
        when(transactionMapper.toResponse(t2)).thenReturn(TransactionResponse.builder().id(2L).amount(BigDecimal.valueOf(200)).build());
        when(transactionMapper.toResponse(t1)).thenReturn(TransactionResponse.builder().id(1L).amount(BigDecimal.valueOf(50)).build());

        TransactionPageResponse result = transactionService.getUserTransactions(userId, null, null, null, null, null, "amount", 0, 10);

        assertNotNull(result);
        assertEquals(2, result.getTransactions().size());
        assertEquals(200, result.getTransactions().get(0).getAmount().intValue());
        assertEquals(50, result.getTransactions().get(1).getAmount().intValue());
    }

    @Test
    void updateTransaction_withCategoryId_shouldUpdateCategory() {
        Long id = 1L;
        Long categoryId = 5L;
        UpdateTransactionRequest request = UpdateTransactionRequest.builder()
                .amount(BigDecimal.valueOf(150))
                .categoryId(categoryId)
                .build();
        Transaction transaction = new Transaction();
        transaction.setId(id);
        Category category = new Category();
        category.setId(categoryId);
        Transaction saved = new Transaction();
        saved.setId(id);
        TransactionResponse response = TransactionResponse.builder().id(id).build();

        when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(transactionRepository.save(transaction)).thenReturn(saved);
        when(transactionMapper.toResponse(saved)).thenReturn(response);

        TransactionResponse result = transactionService.updateTransaction(id, request);

        assertNotNull(result);
        verify(categoryRepository).findById(categoryId);
        verify(transactionRepository).save(transaction);
    }
}
