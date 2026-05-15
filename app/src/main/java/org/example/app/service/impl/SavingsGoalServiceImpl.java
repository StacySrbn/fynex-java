package org.example.app.service.impl;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.SavingsGoalMapper;
import org.example.app.repository.SavingsGoalRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.SavingsGoalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final UserRepository userRepository;
    private final SavingsGoalMapper savingsGoalMapper;

    public SavingsGoalServiceImpl(SavingsGoalRepository savingsGoalRepository,
                                   UserRepository userRepository,
                                   SavingsGoalMapper savingsGoalMapper) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.userRepository = userRepository;
        this.savingsGoalMapper = savingsGoalMapper;
    }

    @Override
    public SavingsGoalResponse createGoal(Long userId, CreateSavingsGoalRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        SavingsGoal goal = new SavingsGoal();
        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setIcon(request.getIcon());
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUser(user);

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return savingsGoalMapper.toResponse(saved);
    }

    @Override
    public List<SavingsGoalResponse> getUserGoals(Long userId) {
        return savingsGoalRepository.findByUserId(userId).stream()
            .map(savingsGoalMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public SavingsGoalResponse getGoalById(Long id) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", id));
        return savingsGoalMapper.toResponse(goal);
    }

    @Override
    public SavingsGoalResponse updateGoal(Long id, UpdateSavingsGoalRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", id));

        if (request.getName() != null) goal.setName(request.getName());
        if (request.getTargetAmount() != null) goal.setTargetAmount(request.getTargetAmount());
        if (request.getIcon() != null) goal.setIcon(request.getIcon());

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return savingsGoalMapper.toResponse(saved);
    }

    @Override
    public SavingsGoalResponse addMoney(Long goalId, AddMoneyRequest request) {
        SavingsGoal goal = savingsGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", goalId));

        goal.setCurrentAmount(goal.getCurrentAmount().add(request.getAmount()));
        SavingsGoal saved = savingsGoalRepository.save(goal);
        return savingsGoalMapper.toResponse(saved);
    }

    @Override
    public void deleteGoal(Long id) {
        if (!savingsGoalRepository.existsById(id)) {
            throw new ResourceNotFoundException("SavingsGoal", id);
        }
        savingsGoalRepository.deleteById(id);
    }
}
