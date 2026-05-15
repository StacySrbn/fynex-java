package org.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.app.dto.ExportRequest;
import org.example.app.dto.ExportResponse;
import org.example.app.entity.User;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.UserRepository;
import org.example.app.service.ExportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exports")
@Tag(name = "Exports", description = "Export management endpoints")
public class ExportController {

    private final ExportService exportService;
    private final UserRepository userRepository;

    public ExportController(ExportService exportService, UserRepository userRepository) {
        this.exportService = exportService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Start export", description = "Starts a new export job for the authenticated user (premium feature)")
    public ResponseEntity<ExportResponse> startExport(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ExportRequest request) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(exportService.startExport(userId, request));
    }

    private Long resolveUserId(Jwt jwt) {
        String email = jwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }
}
