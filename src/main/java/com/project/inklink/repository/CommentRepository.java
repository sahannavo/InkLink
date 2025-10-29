package com.project.inklink.repository;

import com.project.inklink.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByStoryIdAndParentCommentIsNull(Long storyId, Pageable pageable);

    List<Comment> findByParentCommentId(Long parentCommentId);

    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);

    Long countByStoryId(Long storyId);

    Long countByAuthorId(Long authorId);

    @Query("SELECT c FROM Comment c WHERE c.story.id = :storyId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelCommentsByStoryId(@Param("storyId") Long storyId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.story.author.id = :authorId")
    Long countCommentsOnAuthorStories(@Param("authorId") Long authorId);

    @Query("SELECT c FROM Comment c WHERE c.story.id = :storyId ORDER BY c.createdAt ASC")
    List<Comment> findAllByStoryIdOrderByCreatedAt(@Param("storyId") Long storyId);
}