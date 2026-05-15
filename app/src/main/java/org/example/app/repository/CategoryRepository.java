package org.example.app.repository;

import org.example.app.entity.Category;
import org.example.app.entity.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrUserIdIsNull(Long userId);

    List<Category> findByType(CategoryType type);
}
