package com.project.inklink.service;

import com.project.inklink.entity.Comment;
import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final StoryService storyService;
    private final UserService userService;
    private final ValidationService validationService;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository,
                          StoryService storyService,
                          UserService userService,
                          ValidationService validationService,
                          NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.storyService = storyService;
        this.userService = userService;
        this.validationService = validationService;
        this.notificationService = notificationService;
    }

    public Comment createComment(Long storyId, Long userId, String content, Long parentCommentId) {
        Optional<Story> storyOpt = storyService.getStoryById(storyId);
        Optional<User> userOpt = userService.findById(userId);

        if (storyOpt.isEmpty() || userOpt.isEmpty()) {
            throw new IllegalArgumentException("Story or user not found");
        }

        validationService.validateCommentContent(content);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(userOpt.get());
        comment.setStory(storyOpt.get());

        if (parentCommentId != null) {
            Optional<Comment> parentComment = commentRepository.findById(parentCommentId);
            parentComment.ifPresent(comment::setParentComment);
        }

        Comment savedComment = commentRepository.save(comment);

        // Trigger notification
        notificationService.createCommentNotification(savedComment);

        return savedComment;
    }

    public Page<Comment> getCommentsByStory(Long storyId, Pageable pageable) {
        return commentRepository.findByStoryIdAndParentCommentIsNull(storyId, pageable);
    }

    public List<Comment> getReplies(Long parentCommentId) {
        return commentRepository.findByParentCommentId(parentCommentId);
    }

    public Comment updateComment(Long commentId, Long userId, String content) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);

        if (commentOpt.isEmpty()) {
            throw new IllegalArgumentException("Comment not found");
        }

        Comment comment = commentOpt.get();
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own comments");
        }

        validationService.validateCommentContent(content);

        comment.setContent(content);
        comment.setEdited(true);

        return commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, Long userId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);

        if (commentOpt.isEmpty()) {
            throw new IllegalArgumentException("Comment not found");
        }

        Comment comment = commentOpt.get();
        boolean isAuthor = comment.getAuthor().getId().equals(userId);
        boolean isStoryAuthor = comment.getStory().getAuthor().getId().equals(userId);

        if (!isAuthor && !isStoryAuthor) {
            throw new IllegalArgumentException("You can only delete your own comments or comments on your stories");
        }

        // If it's a parent comment, delete all replies first
        if (comment.getParentComment() == null) {
            List<Comment> replies = commentRepository.findByParentCommentId(commentId);
            commentRepository.deleteAll(replies);
        }

        commentRepository.delete(comment);
    }

    public Long getCommentCountForStory(Long storyId) {
        return commentRepository.countByStoryId(storyId);
    }

    public Page<Comment> getUserComments(Long userId, Pageable pageable) {
        return commentRepository.findByAuthorId(userId, pageable);
    }

    public List<Comment> getAllCommentsByStory(Long storyId) {
        return commentRepository.findAllByStoryIdOrderByCreatedAt(storyId);
    }

    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }
}

