package org.example.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.app.dto.CategoryResponse;
import org.example.app.dto.CreateCategoryRequest;
import org.example.app.entity.User;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.repository.UserRepository;
import org.example.app.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category management endpoints")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserRepository userRepository;

    public CategoryController(CategoryService categoryService, UserRepository userRepository) {
        this.categoryService = categoryService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PREMIUM', 'ADMIN')")
    @Operation(summary = "Create custom category", description = "Creates a custom category for the authenticated user (premium feature)")
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCategoryRequest request) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCustomCategory(userId, request));
    }

    @GetMapping
    @Operation(summary = "Get user categories", description = "Returns both system and user custom categories")
    public ResponseEntity<List<CategoryResponse>> getUserCategories(@AuthenticationPrincipal Jwt jwt) {
        Long userId = resolveUserId(jwt);
        return ResponseEntity.ok(categoryService.getUserCategories(userId));
    }

    @GetMapping("/system")
    @Operation(summary = "Get system categories", description = "Returns system-defined categories only")
    public ResponseEntity<List<CategoryResponse>> getSystemCategories() {
        return ResponseEntity.ok(categoryService.getSystemCategories());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category by ID")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Jwt jwt) {
        String email = jwt.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getId();
    }
}
