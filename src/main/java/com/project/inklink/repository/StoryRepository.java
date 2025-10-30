package com.project.inklink.repository;

import com.project.inklink.entity.Story;
import com.project.inklink.enums.StoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {

    // Home Page: Published stories with pagination
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' ORDER BY s.publishedAt DESC")
    Page<Story> findPublishedStoriesWithPagination(Pageable pageable);

    // User Profile: Stories by author with pagination and status filtering
    @Query("SELECT s FROM Story s WHERE s.author.id = :authorId AND " +
            "(:status IS NULL OR s.status = :status) " +
            "ORDER BY s.createdAt DESC")
    Page<Story> findByAuthorWithPagination(@Param("authorId") Long authorId,
                                           @Param("status") StoryStatus status,
                                           Pageable pageable);

    // Category filtering for homepage
    @Query("SELECT s FROM Story s WHERE s.category.name = :categoryName AND s.status = 'PUBLISHED'")
    Page<Story> findByCategory(@Param("categoryName") String categoryName, Pageable pageable);

    // Alternative category method for simple string category
    @Query("SELECT s FROM Story s WHERE s.category.name = :category AND s.status = 'PUBLISHED'")
    Page<Story> findByCategoryName(@Param("category") String category, Pageable pageable);

    // Trending stories (most viewed in last 7 days)
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND s.publishedAt >= :weekAgo " +
            "ORDER BY s.viewCount DESC, s.publishedAt DESC")
    Page<Story> findTrendingStories(@Param("weekAgo") LocalDateTime weekAgo, Pageable pageable);

    // Search stories by title or content
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Story> searchByTitleOrContent(@Param("query") String query, Pageable pageable);

    // FIXED: Added @Query annotation to advancedSearch method
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' " +
            "AND (:query IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (:categoryName IS NULL OR s.category.name = :categoryName) " +
            "AND (:authorId IS NULL OR s.author.id = :authorId) " +
            "AND (:minReadingTime IS NULL OR s.readingTime >= :minReadingTime) " +
            "AND (:maxReadingTime IS NULL OR s.readingTime <= :maxReadingTime) " +
            "AND (:startDate IS NULL OR s.publishedAt >= :startDate) " +
            "AND (:endDate IS NULL OR s.publishedAt <= :endDate)")
    Page<Story> advancedSearch(@Param("query") String query,
                               @Param("categoryName") String categoryName,
                               @Param("authorId") Long authorId,
                               @Param("minReadingTime") Integer minReadingTime,
                               @Param("maxReadingTime") Integer maxReadingTime,
                               @Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate,
                               Pageable pageable);

    // Find stories by multiple categories
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND s.category.name IN :categories")
    Page<Story> findByCategories(@Param("categories") List<String> categories, Pageable pageable);

    // Filter by date range
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

    // Additional useful methods
    List<Story> findByAuthorIdAndStatus(Long authorId, StoryStatus status);

    List<Story> findByStatus(StoryStatus status);

    Page<Story> findByStatus(StoryStatus status, Pageable pageable);

    // Count stories by author and status
    Long countByAuthorIdAndStatus(Long authorId, StoryStatus status);

    // Find popular stories - FIXED: Added date parameter
    @Query(value = "SELECT s.* FROM stories s " +
            "LEFT JOIN reactions r ON s.id = r.story_id AND r.type = 'LIKE' " +
            "WHERE s.status = 'PUBLISHED' AND s.publishedAt >= :since " +
            "GROUP BY s.id " +
            "ORDER BY (s.view_count + COUNT(r.id)) DESC",
            countQuery = "SELECT COUNT(*) FROM stories s WHERE s.status = 'PUBLISHED' AND s.publishedAt >= :since",
            nativeQuery = true)
    Page<Story> findPopularStories(@Param("since") LocalDateTime since, Pageable pageable);

    // Check if user is story owner
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM Story s WHERE s.id = :storyId AND s.author.id = :userId")
    boolean existsByIdAndAuthorId(@Param("storyId") Long storyId, @Param("userId") Long userId);

    // Increment view count
    @Modifying
    @Query("UPDATE Story s SET s.viewCount = s.viewCount + 1 WHERE s.id = :storyId")
    void incrementViewCount(@Param("storyId") Long storyId);

    // Find related stories by category
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND s.category.name = :categoryName AND s.id != :excludeStoryId")
    Page<Story> findRelatedStories(@Param("categoryName") String categoryName,
                                   @Param("excludeStoryId") Long excludeStoryId,
                                   Pageable pageable);
}