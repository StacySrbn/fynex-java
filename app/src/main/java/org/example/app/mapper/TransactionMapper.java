package org.example.app.mapper;

import org.example.app.dto.CreateTransactionRequest;
import org.example.app.dto.TransactionResponse;
import org.example.app.dto.UpdateTransactionRequest;
import org.example.app.entity.Category;
import org.example.app.entity.Transaction;
import org.example.app.entity.TransactionSource;
import org.example.app.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) return null;

        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setDate(transaction.getDate());
        response.setDescription(transaction.getDescription());
        response.setType(transaction.getType());
        response.setSource(transaction.getSource());

        if (transaction.getUser() != null) {
            response.setUserId(transaction.getUser().getId());
        }

        if (transaction.getCategory() != null) {
            response.setCategoryId(transaction.getCategory().getId());
            response.setCategoryName(transaction.getCategory().getName());
            response.setCategoryIcon(transaction.getCategory().getIcon());
        }

        return response;
    }

    public Transaction toEntity(CreateTransactionRequest request, User user, Category category) {
        if (request == null) return null;

        Transaction transaction = new Transaction();
        transaction.setAmount(request.getAmount());
        transaction.setDate(request.getDate());
        transaction.setDescription(request.getDescription());
        transaction.setType(request.getType());
        transaction.setSource(TransactionSource.MANUAL);
        transaction.setUser(user);
        transaction.setCategory(category);
        return transaction;
    }

    public void updateEntity(UpdateTransactionRequest request, Transaction transaction) {
        if (request == null || transaction == null) return;

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getDate() != null) {
            transaction.setDate(request.getDate());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
    }
}
