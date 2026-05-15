package org.example.app.service;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.SavingsGoalMapper;
import org.example.app.repository.SavingsGoalRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.impl.SavingsGoalServiceImpl;
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
class SavingsGoalServiceImplTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SavingsGoalMapper savingsGoalMapper;

    @InjectMocks
    private SavingsGoalServiceImpl savingsGoalService;

    @Test
    void createGoal_shouldCreateWithZeroBalance() {
        Long userId = 1L;
        CreateSavingsGoalRequest request = CreateSavingsGoalRequest.builder()
                .name("New Laptop")
                .targetAmount(BigDecimal.valueOf(2000))
                .icon("laptop")
                .build();
        User user = new User();
        user.setId(userId);
        SavingsGoal goal = new SavingsGoal();
        goal.setName("New Laptop");
        goal.setTargetAmount(BigDecimal.valueOf(2000));
        goal.setCurrentAmount(BigDecimal.ZERO);
        SavingsGoal saved = new SavingsGoal();
        saved.setId(1L);
        saved.setName("New Laptop");
        saved.setCurrentAmount(BigDecimal.ZERO);
        SavingsGoalResponse response = SavingsGoalResponse.builder()
                .id(1L)
                .name("New Laptop")
                .targetAmount(BigDecimal.valueOf(2000))
                .currentAmount(BigDecimal.ZERO)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(savingsGoalRepository.save(any(SavingsGoal.class))).thenReturn(saved);
        when(savingsGoalMapper.toResponse(saved)).thenReturn(response);

        SavingsGoalResponse result = savingsGoalService.createGoal(userId, request);

        assertNotNull(result);
        assertEquals("New Laptop", result.getName());
        assertEquals(BigDecimal.valueOf(2000), result.getTargetAmount());
        assertEquals(BigDecimal.ZERO, result.getCurrentAmount());
        verify(userRepository).findById(userId);
        verify(savingsGoalRepository).save(any(SavingsGoal.class));
        verify(savingsGoalMapper).toResponse(saved);
    }

    @Test
    void addMoney_shouldIncreaseCurrentAmount() {
        Long goalId = 1L;
        AddMoneyRequest request = AddMoneyRequest.builder()
                .amount(BigDecimal.valueOf(500))
                .build();
        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setCurrentAmount(BigDecimal.valueOf(300));
        SavingsGoal saved = new SavingsGoal();
        saved.setId(goalId);
        saved.setCurrentAmount(BigDecimal.valueOf(800));
        SavingsGoalResponse response = SavingsGoalResponse.builder()
                .id(goalId)
                .currentAmount(BigDecimal.valueOf(800))
                .build();

        when(savingsGoalRepository.findById(goalId)).thenReturn(Optional.of(goal));
        when(savingsGoalRepository.save(goal)).thenReturn(saved);
        when(savingsGoalMapper.toResponse(saved)).thenReturn(response);

        SavingsGoalResponse result = savingsGoalService.addMoney(goalId, request);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(800), result.getCurrentAmount());
        verify(savingsGoalRepository).findById(goalId);
        verify(savingsGoalRepository).save(goal);
        verify(savingsGoalMapper).toResponse(saved);
    }

    @Test
    void deleteGoal_shouldDelete() {
        Long id = 1L;
        when(savingsGoalRepository.existsById(id)).thenReturn(true);

        savingsGoalService.deleteGoal(id);

        verify(savingsGoalRepository).existsById(id);
        verify(savingsGoalRepository).deleteById(id);
    }
}
