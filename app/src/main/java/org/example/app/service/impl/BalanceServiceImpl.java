package org.example.app.service.impl;

import org.example.app.dto.BalanceResponse;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.TransactionRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.BalanceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BalanceServiceImpl implements BalanceService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public BalanceServiceImpl(TransactionRepository transactionRepository,
                               UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BalanceResponse calculateBalance(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        BigDecimal income = transactionRepository.sumIncomeByUserId(userId).orElse(BigDecimal.ZERO);
        BigDecimal expense = transactionRepository.sumExpenseByUserId(userId).orElse(BigDecimal.ZERO);

        return new BalanceResponse(income.subtract(expense));
    }
}
