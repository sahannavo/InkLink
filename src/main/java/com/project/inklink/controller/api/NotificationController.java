package com.project.inklink.controller.api;

import com.project.inklink.entity.Notification;
import com.project.inklink.service.NotificationService;
import com.project.inklink.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService,
                                  UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<Notification>> getUserNotifications(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size) {
        var userOpt = userService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Page<Notification> notifications =
                notificationService.getUserNotifications(userOpt.get().getId(), PageRequest.of(page, size));

        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        var userOpt = userService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Long unreadCount = notificationService.getUnreadNotificationCount(userOpt.get().getId());

        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", unreadCount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        var userOpt = userService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        notificationService.markAsRead(notificationId, userOpt.get().getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notification marked as read");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        var userOpt = userService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        notificationService.markAllAsRead(userOpt.get().getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All notifications marked as read");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<Notification>> getNotificationsByType(@PathVariable String type,
                                                                     @AuthenticationPrincipal UserDetails userDetails,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "20") int size) {
        var userOpt = userService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            com.project.inklink.enums.NotificationType notificationType =
                    com.project.inklink.enums.NotificationType.valueOf(type.toUpperCase());

            Page<Notification> notifications =
                    notificationService.getNotificationsByType(userOpt.get().getId(), notificationType, PageRequest.of(page, size));

            return ResponseEntity.ok(notifications);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/cleanup")
    public ResponseEntity<?> cleanupOldNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        var userOpt = userService.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        notificationService.cleanupOldNotificationsForUser(userOpt.get().getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Old notifications cleaned up successfully");

        return ResponseEntity.ok(response);
    }
}