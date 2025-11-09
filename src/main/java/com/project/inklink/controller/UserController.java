package com.project.inklink.controller;

import com.project.inklink.dto.ApiResponse;
import com.project.inklink.entity.User;
import com.project.inklink.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserProfile(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Return user without sensitive data
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            userInfo.put("profilePicture", user.getProfilePicture());
            userInfo.put("createdAt", user.getCreatedAt());

            return ResponseEntity.ok(new ApiResponse(true, "User profile retrieved", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to get user profile: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/stats")
    public ResponseEntity<ApiResponse> getUserStats(@PathVariable Long userId) {
        try {
            // Mock stats - replace with actual service calls
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalViews", 1250);
            stats.put("totalLikes", 342);
            stats.put("storyCount", 2);
            stats.put("followerCount", 87);
            stats.put("commentCount", 23);
            stats.put("viewsToday", 45);
            stats.put("likesToday", 12);

            return ResponseEntity.ok(new ApiResponse(true, "User stats retrieved", stats));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to get user stats: " + e.getMessage()));
        }
    }

    @GetMapping("/{userId}/stories")
    public ResponseEntity<ApiResponse> getUserStories(@PathVariable Long userId) {
        try {
            // This will be handled by StoryController, but adding for API completeness
            return ResponseEntity.ok(new ApiResponse(true, "User stories endpoint", new HashMap<>()));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to get user stories: " + e.getMessage()));
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateUserProfile(@PathVariable Long userId,
                                                         @RequestBody Map<String, String> updates,
                                                         HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (currentUser == null || !currentUser.getId().equals(userId)) {
                return ResponseEntity.status(403)
                        .body(new ApiResponse(false, "Not authorized to update this profile"));
            }

            // Update allowed fields
            if (updates.containsKey("email")) {
                String newEmail = updates.get("email");
                if (!newEmail.equals(currentUser.getEmail()) && userService.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false, "Email already exists"));
                }
                currentUser.setEmail(newEmail);
            }

            if (updates.containsKey("username")) {
                String newUsername = updates.get("username");
                if (!newUsername.equals(currentUser.getUsername()) && userService.findByUsername(newUsername).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false, "Username already exists"));
                }
                currentUser.setUsername(newUsername);
            }

            User updatedUser = userService.updateUser(currentUser);

            // Return updated user info
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", updatedUser.getId());
            userInfo.put("username", updatedUser.getUsername());
            userInfo.put("email", updatedUser.getEmail());
            userInfo.put("role", updatedUser.getRole());

            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update profile: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse> getProfile(HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse(false, "Authentication required"));
            }

            return ResponseEntity.ok(new ApiResponse(true, "Profile retrieved successfully", user));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to retrieve profile: " + e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(@RequestBody Map<String, String> updates,
                                                     HttpServletRequest request) {
        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse(false, "Authentication required"));
            }

            // Update allowed fields
            if (updates.containsKey("email")) {
                String newEmail = updates.get("email");
                if (!newEmail.equals(user.getEmail()) && userService.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false, "Email already exists"));
                }
                user.setEmail(newEmail);
            }

            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", updatedUser));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to update profile: " + e.getMessage()));
        }
    }

    // FIXED: Consistent session handling
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

        // Load user from database
        return userService.getUserById(userId).orElse(null);
    }
}