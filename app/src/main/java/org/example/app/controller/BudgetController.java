package org.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.app.dto.BudgetReportResponse;
import org.example.app.dto.BudgetResponse;
import org.example.app.dto.CreateBudgetRequest;
import org.example.app.entity.User;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.UserRepository;
import org.example.app.service.BudgetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Budget management endpoints")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    public BudgetController(BudgetService budgetService, UserRepository userRepository) {
        this.budgetService = budgetService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @Operation(summary = "Create budget", description = "Creates a new budget for the authenticated user")
    public ResponseEntity<BudgetResponse> createBudget(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBudgetRequest request) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createBudget(userId, request));
    }

    @GetMapping
    @Operation(summary = "List budgets", description = "Returns all budgets for the authenticated user")
    public ResponseEntity<List<BudgetResponse>> listBudgets(@AuthenticationPrincipal Jwt jwt) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.ok(budgetService.getUserBudgets(userId));
    }

    @GetMapping("/{id}/report")
    @Operation(summary = "Get budget report", description = "Returns a detailed report for a specific budget")
    public ResponseEntity<BudgetReportResponse> getBudgetReport(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetReport(id));
    }

    private Long resolveUserId(Jwt jwt) {
        String email = jwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }
}
