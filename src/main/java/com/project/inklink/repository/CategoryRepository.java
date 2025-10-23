package com.project.inklink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.inklink.entity.Category;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}