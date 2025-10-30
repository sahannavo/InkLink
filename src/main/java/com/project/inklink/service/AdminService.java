package com.project.inklink.service;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.enums.StoryStatus;
import com.project.inklink.repository.StoryRepository;
import com.project.inklink.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private EmailService emailService;

    public Page<User> getAllUsers(Pageable pageable, String search) {
        try {
            if (search != null && !search.trim().isEmpty()) {
                return userRepository.searchUsers(search, pageable);
            }
            return userRepository.findAll(pageable);
        } catch (Exception e) {
            logger.error("Error fetching users: {}", e.getMessage(), e);
            // Return empty page instead of throwing exception
            return Page.empty(pageable);
        }
    }

    public Optional<User> getUserById(Long userId) {
        try {
            return userRepository.findById(userId);
        } catch (Exception e) {
            logger.warn("Error fetching user with ID {}: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    public boolean toggleUserStatus(Long userId, Long adminId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean newStatus = !user.getEnabled();
                user.setEnabled(newStatus);
                userRepository.save(user);

                // Log admin action
                logAdminAction(adminId, "TOGGLE_USER_STATUS",
                        String.format("User ID: %d, New Status: %s", userId, newStatus ? "ENABLED" : "DISABLED"));

                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error toggling user status for ID {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    public boolean changeUserRole(Long userId, String newRole, Long adminId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String oldRole = user.getRole();
                user.setRole("ROLE_" + newRole.toUpperCase());
                userRepository.save(user);

                // Log admin action
                logAdminAction(adminId, "CHANGE_USER_ROLE",
                        String.format("User ID: %d, Role: %s -> %s", userId, oldRole, user.getRole()));

                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error changing user role for ID {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    public Page<Story> getAllStories(Pageable pageable, String status, String search) {
        try {
            if (status != null && !status.isEmpty()) {
                try {
                    StoryStatus storyStatus = StoryStatus.valueOf(status.toUpperCase());
                    if (search != null && !search.trim().isEmpty()) {
                        // This would need a custom repository method
                        return filterStoriesByStatusAndSearch(storyStatus, search, pageable);
                    }
                    return storyRepository.findByStatus(storyStatus, pageable);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid story status: {}", status);
                    // Continue with all stories
                }
            }

            if (search != null && !search.trim().isEmpty()) {
                return storyRepository.searchByTitleOrContent(search, pageable);
            }

            return storyRepository.findAll(pageable);
        } catch (Exception e) {
            logger.error("Error fetching stories: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }
    }

    private Page<Story> filterStoriesByStatusAndSearch(StoryStatus status, String search, Pageable pageable) {
        // Fallback implementation - in production, this should be a repository method
        try {
            List<Story> filteredStories = storyRepository.findByStatus(status).stream()
                    .filter(story -> story.getTitle().toLowerCase().contains(search.toLowerCase()) ||
                            (story.getAuthor() != null &&
                                    story.getAuthor().getUsername().toLowerCase().contains(search.toLowerCase())))
                    .collect(Collectors.toList());

            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredStories.size());

            return new PageImpl<>(
                    filteredStories.subList(start, end),
                    pageable,
                    filteredStories.size()
            );
        } catch (Exception e) {
            logger.error("Error in filterStoriesByStatusAndSearch: {}", e.getMessage());
            return Page.empty(pageable);
        }
    }

    public boolean updateStoryStatus(Long storyId, String status, Long adminId) {
        try {
            StoryStatus newStatus = StoryStatus.valueOf(status.toUpperCase());
            Optional<Story> storyOpt = storyRepository.findById(storyId);

            if (storyOpt.isPresent()) {
                Story story = storyOpt.get();
                StoryStatus oldStatus = StoryStatus.valueOf(story.getStatus());
                story.setStatus(newStatus.name());

                if (newStatus == StoryStatus.PUBLISHED && oldStatus != StoryStatus.PUBLISHED) {
                    story.setPublishedAt(LocalDateTime.now());

                    // Notify author if possible
                    try {
                        if (story.getAuthor() != null && story.getAuthor().getEmail() != null) {
                            emailService.sendSimpleMessage(
                                    story.getAuthor().getEmail(),
                                    "Your Story Has Been Published!",
                                    String.format("Congratulations! Your story '%s' has been published and is now live on InkLink.",
                                            story.getTitle())
                            );
                        }
                    } catch (Exception emailException) {
                        logger.warn("Failed to send publication email: {}", emailException.getMessage());
                        // Continue without email notification
                    }
                }

                storyRepository.save(story);

                // Log admin action
                logAdminAction(adminId, "UPDATE_STORY_STATUS",
                        String.format("Story ID: %d, Status: %s -> %s", storyId, oldStatus, newStatus));

                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid story status provided: {}", status);
            return false;
        } catch (Exception e) {
            logger.error("Error updating story status for ID {}: {}", storyId, e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteStory(Long storyId, Long adminId) {
        try {
            Optional<Story> storyOpt = storyRepository.findById(storyId);
            if (storyOpt.isPresent()) {
                Story story = storyOpt.get();

                // Log before deletion
                analyticsService.logStoryDeletion(story);
                logAdminAction(adminId, "DELETE_STORY",
                        String.format("Story ID: %d, Title: %s", storyId, story.getTitle()));

                storyRepository.delete(story);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting story with ID {}: {}", storyId, e.getMessage(), e);
            return false;
        }
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("totalUsers", userRepository.count());
            stats.put("totalStories", storyRepository.count());
            stats.put("publishedStories", storyRepository.findByStatus(StoryStatus.PUBLISHED).size());
            stats.put("draftStories", storyRepository.findByStatus(StoryStatus.DRAFT).size());

            // Get recent activity (last 7 days)
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            Page<Story> recentStories = storyRepository.findByPublishedDateBetween(
                    weekAgo, LocalDateTime.now(), Pageable.unpaged());
            stats.put("recentStories", recentStories.getContent().size());

        } catch (Exception e) {
            logger.error("Error fetching dashboard stats: {}", e.getMessage(), e);
            // Return default values
            stats.put("totalUsers", 0);
            stats.put("totalStories", 0);
            stats.put("publishedStories", 0);
            stats.put("draftStories", 0);
            stats.put("recentStories", 0);
        }

        return stats;
    }

    public List<Map<String, Object>> getRecentActivities(int limit) {
        try {
            // This would typically come from an activity log repository
            // For now, return recent stories as activities
            return storyRepository.findAll(PageRequest.of(0, limit, Sort.by("createdAt").descending()))
                    .stream()
                    .map(story -> {
                        Map<String, Object> activity = new HashMap<>();
                        activity.put("type", "STORY_" + story.getStatus());
                        activity.put("title", story.getTitle());
                        activity.put("author", story.getAuthor() != null ? story.getAuthor().getUsername() : "Unknown");
                        activity.put("timestamp", story.getCreatedAt());
                        activity.put("storyId", story.getId());
                        return activity;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching recent activities: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // FIXED: Added missing logAdminAction method
    private void logAdminAction(Long adminId, String action, String details) {
        logger.info("Admin Action - Admin ID: {}, Action: {}, Details: {}", adminId, action, details);
        // In a real implementation, you might want to persist this to a database
    }
}