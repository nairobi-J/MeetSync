package com.root.meetsync.service;

import com.root.meetsync.dto.Notification.NotificationDTO;
import com.root.meetsync.entity.Notification;
import com.root.meetsync.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    
    Notification createNotification(User user, String title, String message, 
                                   Notification.NotificationType type);
    
    Notification createNotification(User user, String title, String message, 
                                   Notification.NotificationType type, 
                                   Long relatedEntityId, String relatedEntityType, 
                                   String actionUrl);
    
    List<NotificationDTO> getAllNotifications(User user);
    
    Page<NotificationDTO> getAllNotifications(User user, Pageable pageable);
    
    List<NotificationDTO> getUnreadNotifications(User user);
    
    Optional<NotificationDTO> getNotificationById(Long id);
    
    Long getUnreadCount(User user);
    
    Long getPendingCount(User user);
    
    boolean markAsRead(Long notificationId);
    
    int markAllAsRead(User user);
    
    void deleteNotification(Long id);
    
    void deleteAllNotifications(User user);
    
    int deleteOldReadNotifications(User user, int daysOld);
    
    Long getEventNotificationCount(User user);
    
    Long getBookingNotificationCount(User user);
}
