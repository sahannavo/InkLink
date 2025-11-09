package com.project.inklink.controller;

import com.project.inklink.dto.ApiResponse;
import com.project.inklink.dto.CommentRequest;
import com.project.inklink.dto.StoryRequest;
import com.project.inklink.entity.Comment;
import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.entity.enums.StoryGenre;
import com.project.inklink.service.CommentService;
import com.project.inklink.service.StoryService;
import com.project.inklink.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.project.inklink.util.PaginationUtil.createPageable;

@RestController
@RequestMapping("/api/stories")
public class StoryController {

    @Autowired
    private StoryService storyService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;  // ADD THIS LINE - IT WAS MISSING

    // Get all published stories with pagination and filtering
    @GetMapping
    public ResponseEntity<ApiResponse> getStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StoryGenre genre,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        try {
            Pageable pageable = createPageable(page, size, sort);
            Page<Story> stories;

            if (search != null && !search.trim().isEmpty()) {
                stories = storyService.searchStories(search.trim(), pageable);
            } else if (genre != null) {
                stories = storyService.getStoriesByGenre(genre, pageable);
            } else {
                stories = storyService.getPublishedStories(pageable);
            }

            return ResponseEntity.ok(new ApiResponse(true, "Stories retrieved successfully", stories));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve stories: " + e.getMessage()));
        }
    }

    // Get single story
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getStory(@PathVariable Long id, HttpServletRequest request) {
        try {
            Optional<Story> story = storyService.getStoryById(id);
            if (story.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Increment read count if story is published
            if (story.get().getStatus().name().equals("PUBLISHED")) {
                storyService.incrementReadCount(id);
            }

            return ResponseEntity.ok(new ApiResponse(true, "Story retrieved successfully", story.get()));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve story: " + e.getMessage()));
        }
    }

    // Get stories by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getStoriesByUser(@PathVariable Long userId) {
        try {
            // In a real app, you'd have a service method for this
            // For now, return empty array
            return ResponseEntity.ok(new ApiResponse(true, "User stories retrieved", List.of()));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve user stories: " + e.getMessage()));
        }
    }

    // Create new story
    @PostMapping
    public ResponseEntity<ApiResponse> createStory(@Valid @RequestBody StoryRequest storyRequest,
                                                   HttpServletRequest request) {
        try {
            User author = getCurrentUser(request);
            if (author == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse(false, "Authentication required"));
            }

            Story story = new Story();
            story.setTitle(storyRequest.getTitle());
            story.setContent(storyRequest.getContent());
            story.setGenre(storyRequest.getGenre());
            story.setStatus(storyRequest.getStatus());
            story.setAuthor(author);

            Story savedStory = storyService.createStory(story);
            return ResponseEntity.ok(new ApiResponse(true, "Story created successfully", savedStory));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to create story: " + e.getMessage()));
        }
    }

    // Update story
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateStory(@PathVariable Long id,
                                                   @Valid @RequestBody StoryRequest storyRequest,
                                                   HttpServletRequest request) {
        try {
            User author = getCurrentUser(request);
            if (author == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse(false, "Authentication required"));
            }

            Optional<Story> existingStory = storyService.getStoryById(id);
            if (existingStory.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if user is the author
            if (!storyService.isStoryAuthor(id, author.getId())) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Not authorized to update this story"));
            }

            Story story = existingStory.get();
            story.setTitle(storyRequest.getTitle());
            story.setContent(storyRequest.getContent());
            story.setGenre(storyRequest.getGenre());
            story.setStatus(storyRequest.getStatus());

            Story updatedStory = storyService.updateStory(story);
            return ResponseEntity.ok(new ApiResponse(true, "Story updated successfully", updatedStory));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update story: " + e.getMessage()));
        }
    }

    // Delete story
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteStory(@PathVariable Long id, HttpServletRequest request) {
        try {
            User author = getCurrentUser(request);
            if (author == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse(false, "Authentication required"));
            }

            Optional<Story> story = storyService.getStoryById(id);
            if (story.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Check if user is the author
            if (!storyService.isStoryAuthor(id, author.getId())) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Not authorized to delete this story"));
            }

            storyService.deleteStory(id);
            return ResponseEntity.ok(new ApiResponse(true, "Story deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to delete story: " + e.getMessage()));
        }
    }

    // Get user's stories
    @GetMapping("/my")
    public ResponseEntity<ApiResponse> getMyStories(HttpServletRequest request) {
        try {
            User author = getCurrentUser(request);
            if (author == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse(false, "Authentication required"));
            }

            List<Story> stories = storyService.getUserStories(author);
            return ResponseEntity.ok(new ApiResponse(true, "User stories retrieved successfully", stories));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve user stories: " + e.getMessage()));
        }
    }

    // Comment endpoints
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse> getStoryComments(@PathVariable Long id) {
        try {
            Optional<Story> story = storyService.getStoryById(id);
            if (story.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<Comment> comments = commentService.getStoryComments(story.get());
            return ResponseEntity.ok(new ApiResponse(true, "Comments retrieved successfully", comments));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve comments: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse> addComment(@PathVariable Long id,
                                                  @Valid @RequestBody CommentRequest commentRequest,
                                                  HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse(false, "Authentication required"));
            }

            Optional<Story> story = storyService.getStoryById(id);
            if (story.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Comment comment = new Comment();
            comment.setContent(commentRequest.getContent());
            comment.setUser(user);
            comment.setStory(story.get());

            Comment savedComment = commentService.createComment(comment);
            return ResponseEntity.ok(new ApiResponse(true, "Comment added successfully", savedComment));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to add comment: " + e.getMessage()));
        }
    }

    // Utility methods
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        // Get user ID from session (set by AuthController)
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return null;
        }

        // Load user from database using the injected userService
        return userService.getUserById(userId).orElse(null);
    }
}