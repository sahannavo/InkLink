package com.project.inklink.repository;

import com.project.inklink.entity.Story;
import com.project.inklink.enums.StoryStatus;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // Basic methods
    Long countByAuthorIdAndStatus(Long authorId, StoryStatus status);

    // Home Page: "slow profile loop" - Published stories with pagination
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' ORDER BY s.publishedAt DESC")
    Page<Story> findPublishedStoriesWithPagination(Pageable pageable);

    // User Profile: Stories by author with pagination
    @Query("SELECT s FROM Story s WHERE s.author = :author AND s.status = :status")
    Page<Story> findByAuthorWithPagination(@Param("author") User author,
                                           @Param("status") StoryStatus status,
                                           Pageable pageable);

    // Category filtering for homepage
    @Query("SELECT s FROM Story s WHERE s.category.name = :categoryName AND s.status = 'PUBLISHED'")
    Page<Story> findByCategory(@Param("categoryName") String categoryName, Pageable pageable);

    // Trending stories (most viewed in last 7 days)
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND s.publishedAt >= :weekAgo " +
            "ORDER BY s.viewCount DESC, s.publishedAt DESC")
    List<Story> findTrendingStories(@Param("weekAgo") LocalDateTime weekAgo, Pageable pageable);

    // CORRECTED: Fixed table name from 'story' to 'stories'
    @Query(value = "SELECT * FROM stories s WHERE s.status = 'PUBLISHED' AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%')))",
            countQuery = "SELECT COUNT(*) FROM stories s WHERE s.status = 'PUBLISHED' AND " +
                    "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                    "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%')))",
            nativeQuery = true)
    Page<Story> searchByTitleOrContent(@Param("query") String query, Pageable pageable);

    // Filter by date range for "Date time" feature
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND " +
            "s.publishedAt BETWEEN :startDate AND :endDate")
    Page<Story> findByPublishedDateBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    // Filter by reading time
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND " +
            "s.readingTime <= :maxReadingTime")
    Page<Story> findByReadingTimeLessThanEqual(@Param("maxReadingTime") Integer maxReadingTime,
                                               Pageable pageable);

    // Count stories for user profile
    Long countByAuthorAndStatus(User author, StoryStatus status);

    // Find stories by multiple categories
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND s.category.name IN :categories")
    Page<Story> findByCategories(@Param("categories") List<String> categories, Pageable pageable);
}