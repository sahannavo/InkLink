package com.project.inklink.service;

import com.project.inklink.entity.Comment;
import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.entity.enums.UserRole;
import com.project.inklink.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    // Basic CRUD operations
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment getCommentWithDetails(Long id) {
        return commentRepository.findByIdWithUserAndStory(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
    }

    public Comment createComment(Comment comment) {
        // Validate that user hasn't already commented too much on this story
        if (hasUserCommentedRecently(comment.getUser(), comment.getStory())) {
            throw new RuntimeException("You have commented recently on this story. Please wait before commenting again.");
        }

        return commentRepository.save(comment);
    }

    public Comment updateComment(Long commentId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    // Story-related operations
    public List<Comment> getStoryComments(Story story) {
        return commentRepository.findByStoryOrderByCreatedAtDesc(story);
    }

    public Page<Comment> getStoryComments(Story story, Pageable pageable) {
        return commentRepository.findByStory(story, pageable);
    }

    public List<Comment> getStoryCommentsWithUsers(Story story) {
        return commentRepository.findByStoryWithUser(story);
    }

    public Long getStoryCommentCount(Story story) {
        return commentRepository.countByStory(story);
    }

    // User-related operations
    public List<Comment> getUserComments(User user) {
        return commentRepository.findByUser(user);
    }

    public Page<Comment> getUserComments(User user, Pageable pageable) {
        return commentRepository.findByUser(user, pageable);
    }

    public Long getUserCommentCount(User user) {
        return commentRepository.countByUser(user);
    }

    // Authorization checks
    public boolean isCommentAuthor(Long commentId, Long userId) {
        return commentRepository.findById(commentId)
                .map(comment -> comment.getUser().getId().equals(userId))
                .orElse(false);
    }

    public boolean canDeleteComment(Long commentId, User user) {
        return commentRepository.findById(commentId)
                .map(comment ->
                        comment.getUser().getId().equals(user.getId()) ||
                                comment.getStory().getAuthor().getId().equals(user.getId()) ||
                                user.getRole() == UserRole.ADMIN
                )
                .orElse(false);
    }

    // Validation methods
    private boolean hasUserCommentedRecently(User user, Story story) {
        // Check if user has commented on this story in the last hour
        // This is a simple implementation - you might want to make it more sophisticated
        return commentRepository.existsByUserAndStory(user, story);
    }

    // Bulk operations
    public void deleteUserComments(User user) {
        commentRepository.deleteByUser(user);
    }

    public void deleteStoryComments(Story story) {
        commentRepository.deleteByStory(story);
    }

    // Recent activity
    public Page<Comment> getRecentComments(Pageable pageable) {
        return commentRepository.findRecentComments(pageable);
    }

    // Analytics
    public Long getTotalCommentCount() {
        return commentRepository.count();
    }
}