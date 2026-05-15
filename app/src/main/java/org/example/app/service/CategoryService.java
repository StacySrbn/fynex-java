package org.example.app.service;

import org.example.app.dto.*;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCustomCategory(Long userId, CreateCategoryRequest request);

    List<CategoryResponse> getUserCategories(Long userId);

    List<CategoryResponse> getSystemCategories();

    void deleteCategory(Long id);
}
