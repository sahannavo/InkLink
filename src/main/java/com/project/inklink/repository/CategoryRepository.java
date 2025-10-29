package com.project.inklink.repository;

import com.project.inklink.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find category by name
    Optional<Category> findByName(String name);

    // Check if category exists by name
    boolean existsByName(String name);

    // Get all categories sorted by name for dropdowns
    @Query("SELECT c FROM Category c ORDER BY c.name ASC")
    List<Category> findAllOrderByName();

    // Get popular categories (with most stories)
    @Query("SELECT c, COUNT(s) as storyCount FROM Category c LEFT JOIN Story s ON s.category = c AND s.status = 'PUBLISHED' " +
            "GROUP BY c ORDER BY storyCount DESC")
    List<Object[]> findPopularCategories(Pageable pageable);

    // Search categories by name
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Category> findByNameContainingIgnoreCase(@Param("query") String query);

    // Find categories with story count
    @Query("SELECT c, COUNT(s) FROM Category c LEFT JOIN c.stories s WHERE s.status = 'PUBLISHED' GROUP BY c")
    List<Object[]> findAllWithStoryCount();

    // Get categories by story count range
    @Query("SELECT c FROM Category c WHERE (SELECT COUNT(s) FROM c.stories s WHERE s.status = 'PUBLISHED') BETWEEN :min AND :max")
    List<Category> findByStoryCountBetween(@Param("min") Long min, @Param("max")Long max);
}