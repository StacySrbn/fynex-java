package org.example.app.service.impl;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.TransactionMapper;
import org.example.app.repository.*;
import org.example.app.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                   CategoryRepository categoryRepository,
                                   UserRepository userRepository,
                                   TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public TransactionResponse createTransaction(Long userId, CreateTransactionRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Transaction transaction = transactionMapper.toEntity(request, user, category);
        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @Override
    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));

        if (request.getAmount() != null) transaction.setAmount(request.getAmount());
        if (request.getDate() != null) transaction.setDate(request.getDate());
        if (request.getDescription() != null) transaction.setDescription(request.getDescription());
        if (request.getType() != null) transaction.setType(request.getType());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
            transaction.setCategory(category);
        }

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toResponse(saved);
    }

    @Override
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction", id);
        }
        transactionRepository.deleteById(id);
    }

    @Override
    public TransactionResponse getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction", id));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    public TransactionPageResponse getUserTransactions(Long userId, String type, Long categoryId,
                                                        LocalDate dateFrom, LocalDate dateTo,
                                                        String search, String sortBy, int page, int size) {
        List<Transaction> transactions;

        if (dateFrom != null && dateTo != null) {
            transactions = transactionRepository.findByUserIdAndDateBetween(userId, dateFrom, dateTo);
        } else if (type != null) {
            TransactionType txType = TransactionType.valueOf(type.toUpperCase());
            transactions = transactionRepository.findByUserIdAndType(userId, txType);
        } else if (categoryId != null) {
            transactions = transactionRepository.findByUserIdAndCategoryId(userId, categoryId);
        } else {
            transactions = transactionRepository.findByUserIdOrderByDateDesc(userId);
        }

        if (search != null && !search.isEmpty()) {
            transactions = transactions.stream()
                .filter(t -> t.getDescription() != null &&
                    t.getDescription().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
        }

        if ("amount".equals(sortBy)) {
            transactions.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        } else {
            transactions.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        }

        int start = page * size;
        int end = Math.min(start + size, transactions.size());
        List<Transaction> pageContent = start < transactions.size()
            ? transactions.subList(start, end)
            : List.of();

        List<TransactionResponse> responses = pageContent.stream()
            .map(transactionMapper::toResponse)
            .collect(Collectors.toList());

        TransactionPageResponse response = new TransactionPageResponse();
        response.setTransactions(responses);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements(transactions.size());
        response.setTotalPages((int) Math.ceil((double) transactions.size() / size));

        return response;
    }
}
