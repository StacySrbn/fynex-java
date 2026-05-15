package org.example.app.mapper;

import org.example.app.dto.CategoryResponse;
import org.example.app.dto.CreateCategoryRequest;
import org.example.app.entity.Category;
import org.example.app.entity.CategoryType;
import org.example.app.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;

        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setIcon(category.getIcon());
        response.setType(category.getType());

        if (category.getUser() != null) {
            response.setUserId(category.getUser().getId());
        }

        return response;
    }

    public Category toEntity(CreateCategoryRequest request, User user) {
        if (request == null) return null;

        Category category = new Category();
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setType(CategoryType.CUSTOM);
        category.setUser(user);
        return category;
    }
}
