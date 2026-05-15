package org.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.app.dto.FinancialStatisticsResponse;
import org.example.app.dto.StatisticsFilter;
import org.example.app.entity.TransactionType;
import org.example.app.entity.User;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.UserRepository;
import org.example.app.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/statistics")
@Tag(name = "Statistics", description = "Statistics endpoints")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final UserRepository userRepository;

    public StatisticsController(StatisticsService statisticsService, UserRepository userRepository) {
        this.statisticsService = statisticsService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Get statistics", description = "Returns financial statistics with optional filters")
    public ResponseEntity<FinancialStatisticsResponse> getStatistics(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String type) {
        Long userId = resolveUserId(jwt);

        StatisticsFilter filter = StatisticsFilter.builder()
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .type(type != null ? TransactionType.valueOf(type.toUpperCase()) : null)
                .build();

        return ResponseEntity.ok(statisticsService.getStatistics(userId, filter));
    }

    private Long resolveUserId(Jwt jwt) {
        String email = jwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }
}
