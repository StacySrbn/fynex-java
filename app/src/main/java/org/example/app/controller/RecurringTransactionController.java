package org.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.app.dto.CreateRecurringTransactionRequest;
import org.example.app.dto.RecurringTransactionResponse;
import org.example.app.entity.User;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.UserRepository;
import org.example.app.service.RecurringTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
@Tag(name = "Recurring Transactions", description = "Recurring transaction management endpoints")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;
    private final UserRepository userRepository;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService, UserRepository userRepository) {
        this.recurringTransactionService = recurringTransactionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Create recurring transaction", description = "Creates a new recurring transaction (premium feature)")
    public ResponseEntity<RecurringTransactionResponse> createRecurringTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateRecurringTransactionRequest request) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recurringTransactionService.createRecurringTransaction(userId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "List recurring transactions", description = "Returns all recurring transactions for the authenticated user")
    public ResponseEntity<List<RecurringTransactionResponse>> listRecurringTransactions(@AuthenticationPrincipal Jwt jwt) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.ok(recurringTransactionService.getUserRecurring(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Get recurring transaction by ID", description = "Returns a recurring transaction by its ID")
    public ResponseEntity<RecurringTransactionResponse> getRecurringTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(recurringTransactionService.getRecurringById(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Deactivate recurring transaction", description = "Deactivates a recurring transaction by ID")
    public ResponseEntity<Void> deactivateRecurringTransaction(@PathVariable Long id) {
        recurringTransactionService.deactivateRecurring(id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Jwt jwt) {
        String email = jwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }
}
