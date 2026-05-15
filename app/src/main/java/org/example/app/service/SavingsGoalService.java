package org.example.app.service;

import org.example.app.dto.*;

import java.util.List;

public interface SavingsGoalService {

    SavingsGoalResponse createGoal(Long userId, CreateSavingsGoalRequest request);

    List<SavingsGoalResponse> getUserGoals(Long userId);

    SavingsGoalResponse getGoalById(Long id);

    SavingsGoalResponse updateGoal(Long id, UpdateSavingsGoalRequest request);

    SavingsGoalResponse addMoney(Long goalId, AddMoneyRequest request);

    void deleteGoal(Long id);
}
