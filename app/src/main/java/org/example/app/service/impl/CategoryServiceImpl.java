package org.example.app.service.impl;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.CategoryMapper;
import org.example.app.repository.CategoryRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                                UserRepository userRepository,
                                CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryResponse createCustomCategory(Long userId, CreateCategoryRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        Category category = categoryMapper.toEntity(request, user);
        category.setType(CategoryType.CUSTOM);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    public List<CategoryResponse> getUserCategories(Long userId) {
        return categoryRepository.findByUserIdOrUserIdIsNull(userId).stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getSystemCategories() {
        return categoryRepository.findByType(CategoryType.SYSTEM).stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
    }
}
