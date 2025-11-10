package com.project.inklink.controller;

import com.project.inklink.dto.ApiResponse;
import com.project.inklink.entity.Comment;
import com.project.inklink.entity.User;
import com.project.inklink.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/author/{userId}")
    public ResponseEntity<ApiResponse> getCommentsByAuthor(@PathVariable Long userId) {
        try {
            // In a real app, you'd have a service method for this
            // For now, return empty array
            return ResponseEntity.ok(new ApiResponse(true, "Author comments retrieved", List.of()));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve author comments: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse(false, "Authentication required"));
            }

            // Check if user is comment author
            if (!commentService.isCommentAuthor(id, user.getId())) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Not authorized to delete this comment"));
            }

            commentService.deleteComment(id);
            return ResponseEntity.ok(new ApiResponse(true, "Comment deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete comment: " + e.getMessage()));
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user");
    }
}