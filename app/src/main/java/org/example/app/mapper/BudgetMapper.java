package org.example.app.mapper;

import org.example.app.dto.BudgetReportResponse;
import org.example.app.dto.BudgetResponse;
import org.example.app.entity.Budget;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BudgetMapper {

    public BudgetResponse toResponse(Budget budget) {
        if (budget == null) return null;

        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setLimitAmount(budget.getLimitAmount());
        response.setStartDate(budget.getStartDate());
        response.setEndDate(budget.getEndDate());

        if (budget.getCategory() != null) {
            response.setCategoryId(budget.getCategory().getId());
            response.setCategoryName(budget.getCategory().getName());
        }

        if (budget.getUser() != null) {
            response.setUserId(budget.getUser().getId());
        }

        return response;
    }

    public BudgetReportResponse toReportResponse(Budget budget, BigDecimal spent) {
        if (budget == null) return null;

        BudgetReportResponse response = new BudgetReportResponse();
        response.setBudgetId(budget.getId());

        if (budget.getCategory() != null) {
            response.setCategoryName(budget.getCategory().getName());
        }

        response.setLimitAmount(budget.getLimitAmount());
        response.setSpentAmount(spent);

        if (budget.getLimitAmount() != null && spent != null) {
            BigDecimal remaining = budget.getLimitAmount().subtract(spent);
            response.setRemainingAmount(remaining);

            if (budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = spent.multiply(BigDecimal.valueOf(100))
                    .divide(budget.getLimitAmount(), 2, RoundingMode.HALF_UP);
                response.setUsagePercentage(percentage);
            } else {
                response.setUsagePercentage(BigDecimal.ZERO);
            }
        }

        return response;
    }
}
