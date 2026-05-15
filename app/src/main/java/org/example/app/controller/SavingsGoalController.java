package org.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.app.dto.AddMoneyRequest;
import org.example.app.dto.CreateSavingsGoalRequest;
import org.example.app.dto.SavingsGoalResponse;
import org.example.app.dto.UpdateSavingsGoalRequest;
import org.example.app.entity.User;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.UserRepository;
import org.example.app.service.SavingsGoalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
@Tag(name = "Savings Goals", description = "Savings goal management endpoints")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;
    private final UserRepository userRepository;

    public SavingsGoalController(SavingsGoalService savingsGoalService, UserRepository userRepository) {
        this.savingsGoalService = savingsGoalService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Create savings goal", description = "Creates a new savings goal (premium feature)")
    public ResponseEntity<SavingsGoalResponse> createGoal(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateSavingsGoalRequest request) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savingsGoalService.createGoal(userId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "List savings goals", description = "Returns all savings goals for the authenticated user")
    public ResponseEntity<List<SavingsGoalResponse>> listGoals(@AuthenticationPrincipal Jwt jwt) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.ok(savingsGoalService.getUserGoals(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Get savings goal by ID", description = "Returns a savings goal by its ID")
    public ResponseEntity<SavingsGoalResponse> getGoalById(@PathVariable Long id) {
        return ResponseEntity.ok(savingsGoalService.getGoalById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Update savings goal", description = "Updates an existing savings goal by ID")
    public ResponseEntity<SavingsGoalResponse> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSavingsGoalRequest request) {
        return ResponseEntity.ok(savingsGoalService.updateGoal(id, request));
    }

    @PostMapping("/{id}/add-money")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Add money to savings goal", description = "Adds money to a savings goal")
    public ResponseEntity<SavingsGoalResponse> addMoney(
            @PathVariable Long id,
            @Valid @RequestBody AddMoneyRequest request) {
        return ResponseEntity.ok(savingsGoalService.addMoney(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Delete savings goal", description = "Deletes a savings goal by ID")
    public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
        savingsGoalService.deleteGoal(id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Jwt jwt) {
        String email = jwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }
}
