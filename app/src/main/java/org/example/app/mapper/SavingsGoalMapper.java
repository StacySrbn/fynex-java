package org.example.app.mapper;

import org.example.app.dto.SavingsGoalResponse;
import org.example.app.entity.SavingsGoal;
import org.springframework.stereotype.Component;

@Component
public class SavingsGoalMapper {

    public SavingsGoalResponse toResponse(SavingsGoal goal) {
        if (goal == null) return null;

        SavingsGoalResponse response = new SavingsGoalResponse();
        response.setId(goal.getId());
        response.setName(goal.getName());
        response.setTargetAmount(goal.getTargetAmount());
        response.setCurrentAmount(goal.getCurrentAmount());
        response.setIcon(goal.getIcon());
        response.setCreatedAt(goal.getCreatedAt());

        if (goal.getUser() != null) {
            response.setUserId(goal.getUser().getId());
        }

        return response;
    }
}
