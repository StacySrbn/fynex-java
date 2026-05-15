package org.example.app.service.impl;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.RecurringTransactionMapper;
import org.example.app.repository.CategoryRepository;
import org.example.app.repository.RecurringTransactionRepository;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.RecurringTransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RecurringTransactionMapper recurringTransactionMapper;

    public RecurringTransactionServiceImpl(RecurringTransactionRepository recurringTransactionRepository,
                                            TransactionRepository transactionRepository,
                                            CategoryRepository categoryRepository,
                                            UserRepository userRepository,
                                            RecurringTransactionMapper recurringTransactionMapper) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.recurringTransactionMapper = recurringTransactionMapper;
    }

    @Override
    public RecurringTransactionResponse createRecurringTransaction(Long userId, CreateRecurringTransactionRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        RecurringTransaction rt = new RecurringTransaction();
        rt.setTitle(request.getTitle());
        rt.setAmount(request.getAmount());
        rt.setStartDate(request.getStartDate());
        rt.setEndDate(request.getEndDate());
        rt.setRepeatCount(request.getRepeatCount());
        rt.setActive(true);
        rt.setFrequency(request.getFrequency());
        rt.setUser(user);
        rt.setCategory(category);

        RecurringTransaction saved = recurringTransactionRepository.save(rt);
        return recurringTransactionMapper.toResponse(saved);
    }

    @Override
    public List<RecurringTransactionResponse> getUserRecurring(Long userId) {
        return recurringTransactionRepository.findByUserId(userId).stream()
            .map(recurringTransactionMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public RecurringTransactionResponse getRecurringById(Long id) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", id));
        return recurringTransactionMapper.toResponse(rt);
    }

    @Override
    public void deactivateRecurring(Long id) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", id));
        rt.setActive(false);
        recurringTransactionRepository.save(rt);
    }

    @Override
    @Scheduled(cron = "0 0 1 * * *")
    public void generateDueTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueList = recurringTransactionRepository.findActiveForDate(today);

        for (RecurringTransaction rt : dueList) {
            if (!isDueToday(rt, today)) {
                continue;
            }

            Transaction transaction = new Transaction();
            transaction.setAmount(rt.getAmount());
            transaction.setDate(today);
            transaction.setDescription(rt.getTitle());
            transaction.setType(TransactionType.EXPENSE);
            transaction.setSource(TransactionSource.RECURRING);
            transaction.setUser(rt.getUser());
            transaction.setCategory(rt.getCategory());
            transactionRepository.save(transaction);
        }
    }

    private boolean isDueToday(RecurringTransaction rt, LocalDate today) {
        if (rt.getStartDate().isAfter(today)) return false;
        if (rt.getEndDate() != null && rt.getEndDate().isBefore(today)) return false;

        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(rt.getStartDate(), today);
        if (daysBetween < 0) return false;

        switch (rt.getFrequency()) {
            case DAILY:
                return true;
            case WEEKLY:
                return daysBetween % 7 == 0;
            case MONTHLY:
                return rt.getStartDate().getDayOfMonth() == today.getDayOfMonth();
            case YEARLY:
                return rt.getStartDate().getMonth() == today.getMonth()
                    && rt.getStartDate().getDayOfMonth() == today.getDayOfMonth();
            default:
                return false;
        }
    }
}
