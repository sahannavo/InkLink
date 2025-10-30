package com.project.inklink.controller;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class RobustAdminController {

    private static final Logger logger = LoggerFactory.getLogger(RobustAdminController.class);

    private final AdminService adminService;

    public RobustAdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            model.addAllAttributes(stats);
            model.addAttribute("recentActivities", adminService.getRecentActivities(10));
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load dashboard statistics");
            // Set default values
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalStories", 0);
            model.addAttribute("publishedStories", 0);
            model.addAttribute("draftStories", 0);
        }

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String manageUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            Model model) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<User> usersPage = adminService.getAllUsers(pageable, search);

            model.addAttribute("users", usersPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", usersPage.getTotalPages());
            model.addAttribute("totalItems", usersPage.getTotalElements());
            model.addAttribute("search", search);

        } catch (Exception e) {
            model.addAttribute("error", "Unable to load users list");
            model.addAttribute("users", java.util.Collections.emptyList());
        }

        return "admin/users";
    }

    @GetMapping("/stories")
    public String manageStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Story> storiesPage = adminService.getAllStories(pageable, status, search);

            model.addAttribute("stories", storiesPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", storiesPage.getTotalPages());
            model.addAttribute("totalItems", storiesPage.getTotalElements());
            model.addAttribute("status", status);
            model.addAttribute("search", search);

        } catch (Exception e) {
            model.addAttribute("error", "Unable to load stories list");
            model.addAttribute("stories", java.util.Collections.emptyList());
        }

        return "admin/stories";
    }

    @PostMapping("/users/{userId}/toggle-status")
    public String toggleUserStatus(@PathVariable Long userId,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Extract admin ID from user details (you might need to adjust this based on your User entity)
            Long adminId = getCurrentUserId(userDetails);

            boolean success = adminService.toggleUserStatus(userId, adminId);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "User status updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found or unable to update status");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user status: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/users/{userId}/change-role")
    public String changeUserRole(@PathVariable Long userId,
                                 @RequestParam String newRole,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            Long adminId = getCurrentUserId(userDetails);
            boolean success = adminService.changeUserRole(userId, newRole, adminId);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "User role updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found or unable to update role");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user role: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/stories/{storyId}/update-status")
    public String updateStoryStatus(@PathVariable Long storyId,
                                    @RequestParam String status,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            Long adminId = getCurrentUserId(userDetails);
            boolean success = adminService.updateStoryStatus(storyId, status, adminId);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "Story status updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Story not found or invalid status");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating story status: " + e.getMessage());
        }

        return "redirect:/admin/stories";
    }

    @PostMapping("/stories/{storyId}/delete")
    public String deleteStory(@PathVariable Long storyId,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            Long adminId = getCurrentUserId(userDetails);
            boolean success = adminService.deleteStory(storyId, adminId);

            if (success) {
                redirectAttributes.addFlashAttribute("success", "Story deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Story not found or unable to delete");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting story: " + e.getMessage());
        }

        return "redirect:/admin/stories";
    }

    @GetMapping("/analytics")
    public String viewAnalytics(Model model) {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            model.addAllAttributes(stats);
            model.addAttribute("recentActivities", adminService.getRecentActivities(20));
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load analytics data");
        }

        return "admin/analytics";
    }

    @ExceptionHandler(Exception.class)
    public String handleAdminExceptions(Exception e, RedirectAttributes redirectAttributes) {
        logger.error("Admin controller error: {}", e.getMessage(), e);
        redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again.");
        return "redirect:/admin/dashboard";
    }

    // Helper method to extract user ID - you'll need to implement this based on your User entity
    private Long getCurrentUserId(UserDetails userDetails) {
        try {
            // This is a placeholder - implement based on your authentication setup
            // You might need to fetch the User entity from the database
            return 1L; // Default admin ID for demonstration
        } catch (Exception e) {
            logger.warn("Unable to get current user ID: {}", e.getMessage());
            return 1L; // Fallback to default admin ID
        }
    }
}