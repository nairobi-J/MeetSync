package com.root.meetsync.service.impl;

import com.root.meetsync.dto.Notification.NotificationDTO;
import com.root.meetsync.entity.Notification;
import com.root.meetsync.entity.User;
import com.root.meetsync.repository.NotificationRepository;
import com.root.meetsync.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    @Override
    @Transactional
    public Notification createNotification(User user, String title, String message, 
                                          Notification.NotificationType type) {
        return createNotification(user, title, message, type, null, null, null);
    }
    
    @Override
    @Transactional
    public Notification createNotification(User user, String title, String message, 
                                          Notification.NotificationType type,
                                          Long relatedEntityId, String relatedEntityType, 
                                          String actionUrl) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setActionUrl(actionUrl);
        
        return notificationRepository.save(notification);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(notification -> {
                    NotificationDTO dto = NotificationDTO.fromEntity(notification);
                    dto.setTimeAgo(calculateTimeAgo(notification.getCreatedAt()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getAllNotifications(User user, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return notifications.map(notification -> {
            NotificationDTO dto = NotificationDTO.fromEntity(notification);
            dto.setTimeAgo(calculateTimeAgo(notification.getCreatedAt()));
            return dto;
        });
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(notification -> {
                    NotificationDTO dto = NotificationDTO.fromEntity(notification);
                    dto.setTimeAgo(calculateTimeAgo(notification.getCreatedAt()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<NotificationDTO> getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    NotificationDTO dto = NotificationDTO.fromEntity(notification);
                    dto.setTimeAgo(calculateTimeAgo(notification.getCreatedAt()));
                    return dto;
                });
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
    
    @Override
    @Transactional
    public boolean markAsRead(Long notificationId) {
        int updated = notificationRepository.markAsRead(notificationId, LocalDateTime.now());
        return updated > 0;
    }
    
    @Override
    @Transactional
    public int markAllAsRead(User user) {
        return notificationRepository.markAllAsRead(user, LocalDateTime.now());
    }
    
    @Override
    @Transactional
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteAllNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        notificationRepository.deleteAll(notifications);
    }
    
    @Override
    @Transactional
    public int deleteOldReadNotifications(User user, int daysOld) {
        LocalDateTime beforeDate = LocalDateTime.now().minusDays(daysOld);
        return notificationRepository.deleteOldReadNotifications(user, beforeDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getEventNotificationCount(User user) {
        return notificationRepository.countEventNotifications(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getBookingNotificationCount(User user) {
        return notificationRepository.countBookingNotifications(user);
    }
    
    private String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = duration.getSeconds();
        
        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        } else if (seconds < 2592000) {
            long weeks = seconds / 604800;
            return weeks + (weeks == 1 ? " week ago" : " weeks ago");
        } else if (seconds < 31536000) {
            long months = seconds / 2592000;
            return months + (months == 1 ? " month ago" : " months ago");
        } else {
            long years = seconds / 31536000;
            return years + (years == 1 ? " year ago" : " years ago");
        }
    }
}
