package org.example.app.service;

import org.example.app.dto.*;

import java.util.List;

public interface RecurringTransactionService {

    RecurringTransactionResponse createRecurringTransaction(Long userId, CreateRecurringTransactionRequest request);

    List<RecurringTransactionResponse> getUserRecurring(Long userId);

    RecurringTransactionResponse getRecurringById(Long id);

    void deactivateRecurring(Long id);

    void generateDueTransactions();
}
