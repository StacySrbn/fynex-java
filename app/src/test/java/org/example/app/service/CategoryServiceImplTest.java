package org.example.app.service;

import org.example.app.dto.*;
import org.example.app.entity.*;
import org.example.app.exception.ResourceNotFoundException;
import org.example.app.mapper.CategoryMapper;
import org.example.app.repository.CategoryRepository;
import org.example.app.repository.UserRepository;
import org.example.app.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCustomCategory_shouldCreateAndReturnResponse() {
        Long userId = 1L;
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Custom Cat")
                .icon("star")
                .build();
        User user = new User();
        user.setId(userId);
        Category category = new Category();
        category.setName("Custom Cat");
        Category saved = new Category();
        saved.setId(10L);
        saved.setName("Custom Cat");
        CategoryResponse response = CategoryResponse.builder()
                .id(10L)
                .name("Custom Cat")
                .type(CategoryType.CUSTOM)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryMapper.toEntity(request, user)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(response);

        CategoryResponse result = categoryService.createCustomCategory(userId, request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Custom Cat", result.getName());
        assertEquals(CategoryType.CUSTOM, result.getType());
        verify(userRepository).findById(userId);
        verify(categoryMapper).toEntity(request, user);
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(saved);
    }

    @Test
    void getUserCategories_shouldReturnList() {
        Long userId = 1L;
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("Food");
        Category c2 = new Category();
        c2.setId(2L);
        c2.setName("Transport");
        List<Category> categories = List.of(c1, c2);

        when(categoryRepository.findByUserIdOrUserIdIsNull(userId)).thenReturn(categories);
        when(categoryMapper.toResponse(c1)).thenReturn(CategoryResponse.builder().id(1L).name("Food").build());
        when(categoryMapper.toResponse(c2)).thenReturn(CategoryResponse.builder().id(2L).name("Transport").build());

        List<CategoryResponse> result = categoryService.getUserCategories(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Food", result.get(0).getName());
        verify(categoryRepository).findByUserIdOrUserIdIsNull(userId);
    }

    @Test
    void getSystemCategories_shouldReturnList() {
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("Salary");
        c1.setType(CategoryType.SYSTEM);
        Category c2 = new Category();
        c2.setId(2L);
        c2.setName("Rent");
        c2.setType(CategoryType.SYSTEM);
        List<Category> categories = List.of(c1, c2);

        when(categoryRepository.findByType(CategoryType.SYSTEM)).thenReturn(categories);
        when(categoryMapper.toResponse(c1)).thenReturn(CategoryResponse.builder().id(1L).name("Salary").type(CategoryType.SYSTEM).build());
        when(categoryMapper.toResponse(c2)).thenReturn(CategoryResponse.builder().id(2L).name("Rent").type(CategoryType.SYSTEM).build());

        List<CategoryResponse> result = categoryService.getSystemCategories();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(categoryRepository).findByType(CategoryType.SYSTEM);
    }

    @Test
    void deleteCategory_shouldDelete() {
        Long id = 1L;
        when(categoryRepository.existsById(id)).thenReturn(true);

        categoryService.deleteCategory(id);

        verify(categoryRepository).existsById(id);
        verify(categoryRepository).deleteById(id);
    }
}
