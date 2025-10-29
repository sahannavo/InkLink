package com.project.inklink.service;

import com.project.inklink.entity.Comment;
import com.project.inklink.entity.Notification;
import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.enums.NotificationType;
import com.project.inklink.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository,
                               UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    public void createStoryLikeNotification(Story story, User likedBy) {
        if (story.getAuthor().getId().equals(likedBy.getId())) {
            return; // Don't notify if user liked their own story
        }

        Notification notification = new Notification();
        notification.setType(NotificationType.STORY_LIKE);
        notification.setRecipient(story.getAuthor());
        notification.setTriggeredBy(likedBy);
        notification.setStory(story);

        notificationRepository.save(notification);
    }

    public void createCommentNotification(Comment comment) {
        Story story = comment.getStory();
        User storyAuthor = story.getAuthor();
        User commentAuthor = comment.getAuthor();

        // Notify story author about new comment (unless they commented themselves)
        if (!storyAuthor.getId().equals(commentAuthor.getId())) {
            Notification notification = new Notification();
            notification.setType(NotificationType.STORY_COMMENT);
            notification.setRecipient(storyAuthor);
            notification.setTriggeredBy(commentAuthor);
            notification.setStory(story);
            notification.setComment(comment);

            notificationRepository.save(notification);
        }

        // Notify parent comment author about reply
        if (comment.getParentComment() != null) {
            User parentCommentAuthor = comment.getParentComment().getAuthor();
            if (!parentCommentAuthor.getId().equals(commentAuthor.getId())) {
                Notification replyNotification = new Notification();
                replyNotification.setType(NotificationType.COMMENT_REPLY);
                replyNotification.setRecipient(parentCommentAuthor);
                replyNotification.setTriggeredBy(commentAuthor);
                replyNotification.setStory(story);
                replyNotification.setComment(comment);

                notificationRepository.save(replyNotification);
            }
        }
    }

    public void createNewFollowerNotification(User follower, User following) {
        Notification notification = new Notification();
        notification.setType(NotificationType.NEW_FOLLOWER);
        notification.setRecipient(following);
        notification.setTriggeredBy(follower);

        notificationRepository.save(notification);
    }

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getRecipient().getId().equals(userId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications =
                notificationRepository.findByRecipientIdAndReadFalse(userId);

        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOldNotifications() {
        // Get all users and delete old notifications for each
        // For simplicity, we'll delete all notifications older than 30 days
        // In a real scenario, you might want to get all user IDs and call deleteOldNotifications for each
        List<User> allUsers = userService.findAllUsers();
        for (User user : allUsers) {
            try {
                notificationRepository.deleteOldNotifications(user.getId());
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Error cleaning up notifications for user " + user.getId() + ": " + e.getMessage());
            }
        }
    }

    public Page<Notification> getNotificationsByType(Long userId, NotificationType type, Pageable pageable) {
        return notificationRepository.findByRecipientIdAndType(userId, type, pageable);
    }

    // Helper method to cleanup notifications for a specific user
    public void cleanupOldNotificationsForUser(Long userId) {
        notificationRepository.deleteOldNotifications(userId);
    }
}