package com.project.inklink.repository;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.entity.enums.StoryGenre;
import com.project.inklink.entity.enums.StoryStatus;
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

    // Find stories by status with pagination
    Page<Story> findByStatus(StoryStatus status, Pageable pageable);

    // Find stories by author
    List<Story> findByAuthor(User author);

    // Find stories by author with pagination
    Page<Story> findByAuthor(User author, Pageable pageable);

    // Find stories by author and status
    List<Story> findByAuthorAndStatus(User author, StoryStatus status);

    // Find stories by genre and status with pagination
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND s.genre = :genre")
    Page<Story> findByGenreAndStatus(@Param("genre") String genre, Pageable pageable);

    // Search published stories by title or content
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND " +
            "(LOWER(s.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Story> searchPublishedStories(@Param("search") String search, Pageable pageable);

    // Find most popular stories (by read count)
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' ORDER BY s.readCount DESC")
    Page<Story> findPopularStories(Pageable pageable);

    // Find recent stories
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' ORDER BY s.createdAt DESC")
    Page<Story> findRecentStories(Pageable pageable);

    // Find stories by multiple genres
    @Query("SELECT s FROM Story s WHERE s.status = 'PUBLISHED' AND s.genre IN :genres")
    Page<Story> findByGenres(@Param("genres") List<StoryGenre> genres, Pageable pageable);

    // Find stories with tag
    @Query("SELECT s FROM Story s JOIN s.tags t WHERE s.status = 'PUBLISHED' AND t.name = :tagName")
    Page<Story> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    // Find stories created after a certain date
    Page<Story> findByStatusAndCreatedAtAfter(StoryStatus status, LocalDateTime date, Pageable pageable);

    // Count stories by author
    @Query("SELECT COUNT(s) FROM Story s WHERE s.author = :author AND s.status = 'PUBLISHED'")
    Long countPublishedStoriesByAuthor(@Param("author") User author);

    // Find stories with comment count
    @Query("SELECT s, COUNT(c) as commentCount FROM Story s LEFT JOIN s.comments c WHERE s.status = 'PUBLISHED' GROUP BY s ORDER BY commentCount DESC")
    Page<Object[]> findStoriesWithCommentCount(Pageable pageable);

    // Check if story exists by title and author (for duplicate prevention)
    Boolean existsByTitleAndAuthor(String title, User author);

    // Find stories by status with custom sorting
    @Query("SELECT s FROM Story s WHERE s.status = :status")
    Page<Story> findByStatusWithCustomSort(@Param("status") StoryStatus status, Pageable pageable);
}