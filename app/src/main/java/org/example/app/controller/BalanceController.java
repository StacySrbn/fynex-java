package org.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.app.dto.BalanceResponse;
import org.example.app.entity.User;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.UserRepository;
import org.example.app.service.BalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/balance")
@Tag(name = "Balance", description = "Balance endpoints")
public class BalanceController {

    private final BalanceService balanceService;
    private final UserRepository userRepository;

    public BalanceController(BalanceService balanceService, UserRepository userRepository) {
        this.balanceService = balanceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Get current balance", description = "Returns the current balance for the authenticated user")
    public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal Jwt jwt) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.ok(balanceService.calculateBalance(userId));
    }

    private Long resolveUserId(Jwt jwt) {
        String email = jwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }
}
