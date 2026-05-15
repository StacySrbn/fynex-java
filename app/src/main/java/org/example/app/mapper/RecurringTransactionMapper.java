package org.example.app.mapper;

import org.example.app.dto.RecurringTransactionResponse;
import org.example.app.entity.RecurringTransaction;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionMapper {

    public RecurringTransactionResponse toResponse(RecurringTransaction rt) {
        if (rt == null) return null;

        RecurringTransactionResponse response = new RecurringTransactionResponse();
        response.setId(rt.getId());
        response.setTitle(rt.getTitle());
        response.setAmount(rt.getAmount());
        response.setStartDate(rt.getStartDate());
        response.setEndDate(rt.getEndDate());
        response.setRepeatCount(rt.getRepeatCount());
        response.setActive(rt.isActive());
        response.setFrequency(rt.getFrequency());

        if (rt.getUser() != null) {
            response.setUserId(rt.getUser().getId());
        }

        if (rt.getCategory() != null) {
            response.setCategoryId(rt.getCategory().getId());
            response.setCategoryName(rt.getCategory().getName());
        }

        return response;
    }
}
