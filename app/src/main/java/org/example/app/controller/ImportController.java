package org.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.app.dto.ImportResponse;
import org.example.app.entity.User;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.UserRepository;
import org.example.app.service.ImportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imports")
@Tag(name = "Imports", description = "Import management endpoints")
public class ImportController {

    private final ImportService importService;
    private final UserRepository userRepository;

    public ImportController(ImportService importService, UserRepository userRepository) {
        this.importService = importService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload CSV file", description = "Uploads and parses a CSV file with transactions")
    public ResponseEntity<ImportResponse> uploadCsv(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam("file") MultipartFile file) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(importService.uploadCsv(userId, file));
    }

    @PostMapping("/{importId}/confirm")
    @Operation(summary = "Confirm import", description = "Confirms and processes a parsed import")
    public ResponseEntity<ImportResponse> confirmImport(@PathVariable Long importId) {
        return ResponseEntity.ok(importService.confirmImport(importId));
    }

    private Long resolveUserId(Jwt jwt) {
        String email = jwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }
}
