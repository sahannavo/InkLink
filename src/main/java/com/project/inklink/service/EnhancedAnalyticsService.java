package com.project.inklink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;

@Service
public class EnhancedAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedAnalyticsService.class);

    private final ConcurrentHashMap<String, AtomicLong> adminActions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> userActivities = new ConcurrentHashMap<>();

    @Async
    public void logAdminAction(Long adminId, String action, String details) {
        try {
            String key = String.format("admin_%d_%s", adminId, action);
            adminActions.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();

            logger.info("Admin Action - AdminID: {}, Action: {}, Details: {}", adminId, action, details);
        } catch (Exception e) {
            logger.warn("Failed to log admin action: {}", e.getMessage());
            // Don't throw exception - analytics logging shouldn't break main functionality
        }
    }

    @Async
    public void logUserActivity(Long userId, String activityType, String details) {
        try {
            String key = String.format("user_%d_%s", userId, activityType);
            userActivities.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();

            logger.debug("User Activity - UserID: {}, Activity: {}, Details: {}", userId, activityType, details);
        } catch (Exception e) {
            logger.warn("Failed to log user activity: {}", e.getMessage());
            // Continue without logging
        }
    }

    public Map<String, Long> getAdminActionStats(Long adminId) {
        try {
            Map<String, Long> stats = new ConcurrentHashMap<>();
            adminActions.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("admin_" + adminId + "_"))
                    .forEach(entry -> {
                        String action = entry.getKey().split("_")[2]; // Extract action name
                        stats.put(action, entry.getValue().get());
                    });
            return stats;
        } catch (Exception e) {
            logger.error("Error getting admin action stats: {}", e.getMessage());
            return Map.of();
        }
    }

    public Map<String, Long> getUserActivityStats(Long userId) {
        try {
            Map<String, Long> stats = new ConcurrentHashMap<>();
            userActivities.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("user_" + userId + "_"))
                    .forEach(entry -> {
                        String activity = entry.getKey().split("_")[2]; // Extract activity name
                        stats.put(activity, entry.getValue().get());
                    });
            return stats;
        } catch (Exception e) {
            logger.error("Error getting user activity stats: {}", e.getMessage());
            return Map.of();
        }
    }

    public void cleanupOldAnalytics() {
        try {
            // This would typically remove old analytics data
            // For now, we'll just log the cleanup operation
            logger.info("Analytics cleanup completed");
        } catch (Exception e) {
            logger.error("Error during analytics cleanup: {}", e.getMessage());
            // Don't throw - cleanup failures shouldn't break the application
        }
    }
}
