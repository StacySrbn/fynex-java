package org.example.app.service;

import org.example.app.dto.BalanceResponse;
import org.example.app.entity.User;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.impl.BalanceServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    @Test
    void calculateBalance_shouldReturnIncomeMinusExpense() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(transactionRepository.sumIncomeByUserId(userId)).thenReturn(Optional.of(BigDecimal.valueOf(5000)));
        when(transactionRepository.sumExpenseByUserId(userId)).thenReturn(Optional.of(BigDecimal.valueOf(3200)));

        BalanceResponse result = balanceService.calculateBalance(userId);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1800), result.getBalance());
        verify(userRepository).existsById(userId);
        verify(transactionRepository).sumIncomeByUserId(userId);
        verify(transactionRepository).sumExpenseByUserId(userId);
    }

    @Test
    void calculateBalance_whenUserNotFound_shouldThrow() {
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> balanceService.calculateBalance(userId));

        verify(userRepository).existsById(userId);
        verify(transactionRepository, never()).sumIncomeByUserId(any());
        verify(transactionRepository, never()).sumExpenseByUserId(any());
    }

    @Test
    void calculateBalance_whenNoIncome_shouldTreatAsZero() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(transactionRepository.sumIncomeByUserId(userId)).thenReturn(Optional.empty());
        when(transactionRepository.sumExpenseByUserId(userId)).thenReturn(Optional.of(BigDecimal.valueOf(500)));

        BalanceResponse result = balanceService.calculateBalance(userId);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(-500), result.getBalance());
    }

    @Test
    void calculateBalance_whenNoExpense_shouldTreatAsZero() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(transactionRepository.sumIncomeByUserId(userId)).thenReturn(Optional.of(BigDecimal.valueOf(1000)));
        when(transactionRepository.sumExpenseByUserId(userId)).thenReturn(Optional.empty());

        BalanceResponse result = balanceService.calculateBalance(userId);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000), result.getBalance());
    }
}
