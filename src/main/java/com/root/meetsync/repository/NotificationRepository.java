package com.root.meetsync.repository;

import com.root.meetsync.entity.Notification;
import com.root.meetsync.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a user
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // Find all notifications for a user with pagination
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find unread notifications for a user
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Count unread notifications for a user
    Long countByUserAndIsReadFalse(User user);
    
    // Find notifications by type
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, Notification.NotificationType type);
    
    // Find notifications created after a specific date
    List<Notification> findByUserAndCreatedAtAfterOrderByCreatedAtDesc(User user, LocalDateTime date);
    
    // Mark notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :notificationId")
    int markAsRead(@Param("notificationId") Long notificationId, @Param("readAt") LocalDateTime readAt);
    
    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user = :user AND n.isRead = false")
    int markAllAsRead(@Param("user") User user, @Param("readAt") LocalDateTime readAt);
    
    // Delete old read notifications
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user = :user AND n.isRead = true AND n.readAt < :beforeDate")
    int deleteOldReadNotifications(@Param("user") User user, @Param("beforeDate") LocalDateTime beforeDate);
    
    // Count event notifications
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND (n.type = 'EVENT_CREATED' OR n.type = 'EVENT_UPDATED' OR n.type = 'EVENT_CANCELLED')")
    Long countEventNotifications(@Param("user") User user);
    
    // Count booking notifications
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND (n.type = 'BOOKING_PENDING' OR n.type = 'BOOKING_CONFIRMED' OR n.type = 'BOOKING_CANCELLED')")
    Long countBookingNotifications(@Param("user") User user);
}
