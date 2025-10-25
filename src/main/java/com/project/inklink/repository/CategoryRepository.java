package com.project.inklink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.inklink.entity.Category;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);

    // Get all categories sorted by name for dropdowns
    @Query("SELECT c FROM Category c ORDER BY c.name ASC")
    List<Category> findAllOrderByName();

    // Get popular categories (with most stories)
    @Query("SELECT c, COUNT(s) as storyCount FROM Category c LEFT JOIN Story s ON s.category = c AND s.status = 'PUBLISHED' " +
            "GROUP BY c ORDER BY storyCount DESC")
    List<Object[]> findPopularCategories(org.springframework.data.domain.Pageable pageable);

    // Search categories by name
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Category> findByNameContainingIgnoreCase(@Param("query") String query);
}