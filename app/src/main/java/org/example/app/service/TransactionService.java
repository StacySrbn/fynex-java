package org.example.app.service;

import org.example.app.dto.*;

import java.time.LocalDate;

public interface TransactionService {

    TransactionResponse createTransaction(Long userId, CreateTransactionRequest request);

    TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request);

    void deleteTransaction(Long id);

    TransactionResponse getTransactionById(Long id);

    TransactionPageResponse getUserTransactions(Long userId, String type, Long categoryId, LocalDate dateFrom, LocalDate dateTo, String search, String sortBy, int page, int size);
}
