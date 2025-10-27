package com.project.inklink.repository;

import com.project.inklink.entity.Story;
import com.project.inklink.enums.StoryStatus;
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

    // Home Page: Published stories with pagination
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' ORDER BY s.publishedAt DESC")
    Page<Story> findPublishedStoriesWithPagination(Pageable pageable);

    // User Profile: Stories by author with pagination
    @Query("SELECT s FROM Story s WHERE s.author.id = :authorId AND s.status = :status")
    Page<Story> findByAuthorWithPagination(@Param("authorId") Long authorId,
                                           @Param("status") StoryStatus status,
                                           Pageable pageable);

    // Category filtering for homepage
    @Query("SELECT s FROM Story s WHERE s.category.name = :categoryName AND s.status = 'PUBLISHED'")
    Page<Story> findByCategory(@Param("categoryName") String categoryName, Pageable pageable);

    // Trending stories (most viewed in last 7 days)
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND s.publishedAt >= :weekAgo " +
            "ORDER BY s.viewCount DESC, s.publishedAt DESC")
    List<Story> findTrendingStories(@Param("weekAgo") LocalDateTime weekAgo, Pageable pageable);

    // Search stories by title or content
    @Query(value = "SELECT * FROM stories s WHERE s.status = 'PUBLISHED' AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(CAST(s.content AS VARCHAR(10000))) LIKE LOWER(CONCAT('%', :query, '%')))",
            countQuery = "SELECT COUNT(*) FROM stories s WHERE s.status = 'PUBLISHED' AND " +
                    "(LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                    "LOWER(CAST(s.content AS VARCHAR(10000))) LIKE LOWER(CONCAT('%', :query, '%')))",
            nativeQuery = true)
    Page<Story> searchByTitleOrContent(@Param("query") String query, Pageable pageable);

    // Advanced search with multiple filters
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' " +
            "AND (:categoryName IS NULL OR s.category.name = :categoryName) " +
            "AND (:authorId IS NULL OR s.author.id = :authorId) " +
            "AND (:minReadingTime IS NULL OR s.readingTime >= :minReadingTime) " +
            "AND (:maxReadingTime IS NULL OR s.readingTime <= :maxReadingTime) " +
            "AND (:startDate IS NULL OR s.publishedAt >= :startDate) " +
            "AND (:endDate IS NULL OR s.publishedAt <= :endDate)")
    Page<Story> advancedSearch(@Param("categoryName") String categoryName,
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

    // Find stories with high engagement (likes and views)
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' " +
            "ORDER BY (SELECT COUNT(r) FROM Reaction r WHERE r.story = s AND r.type = 'LIKE') DESC, s.viewCount DESC")
    Page<Story> findPopularStories(Pageable pageable);
}