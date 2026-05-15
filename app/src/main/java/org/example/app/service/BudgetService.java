package org.example.app.service;

import org.example.app.dto.*;

import java.util.List;

public interface BudgetService {

    BudgetResponse createBudget(Long userId, CreateBudgetRequest request);

    List<BudgetResponse> getUserBudgets(Long userId);

    BudgetReportResponse getBudgetReport(Long budgetId);
}
