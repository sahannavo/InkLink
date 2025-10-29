package com.project.inklink.repository;

import com.project.inklink.entity.Notification;
import com.project.inklink.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndReadFalse(Long recipientId);

    Long countByRecipientIdAndReadFalse(Long recipientId);

    // FIXED: Using native query for proper date arithmetic
    @Modifying
    @Query(value = "DELETE FROM notifications WHERE recipient_id = :userId AND created_at < CURRENT_DATE - INTERVAL 30 DAY",
            nativeQuery = true)
    void deleteOldNotifications(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.type = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientIdAndType(@Param("userId") Long userId,
                                                @Param("type") NotificationType type,
                                                Pageable pageable);
}