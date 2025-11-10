package com.project.inklink.repository;

import com.project.inklink.entity.Comment;
import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find comments by story, ordered by creation date (newest first)
    List<Comment> findByStoryOrderByCreatedAtDesc(Story story);

    // Find comments by story with pagination
    Page<Comment> findByStory(Story story, Pageable pageable);

    // Find comments by user
    List<Comment> findByUser(User user);

    // Find comments by user with pagination
    Page<Comment> findByUser(User user, Pageable pageable);

    // Find comments by user and story
    List<Comment> findByUserAndStory(User user, Story story);

    // Count comments by story
    Long countByStory(Story story);

    // Count comments by user
    Long countByUser(User user);

    // Find recent comments across all stories
    @Query("SELECT c FROM Comment c ORDER BY c.createdAt DESC")
    Page<Comment> findRecentComments(Pageable pageable);

    // Find comments with user details for a story
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.story = :story ORDER BY c.createdAt DESC")
    List<Comment> findByStoryWithUser(@Param("story") Story story);

    // Find comments created after a certain date
    List<Comment> findByCreatedAtAfter(LocalDateTime date);

    // Find comments by multiple stories
    @Query("SELECT c FROM Comment c WHERE c.story IN :stories ORDER BY c.createdAt DESC")
    List<Comment> findByStories(@Param("stories") List<Story> stories);

    // Delete all comments by a user
    void deleteByUser(User user);

    // Delete all comments for a story
    void deleteByStory(Story story);

    // Check if user has commented on a story
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Comment c WHERE c.user = :user AND c.story = :story")
    Boolean existsByUserAndStory(@Param("user") User user, @Param("story") Story story);

    // Find comment with user and story details
    @Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.story WHERE c.id = :id")
    Optional<Comment> findByIdWithUserAndStory(@Param("id") Long id);
}