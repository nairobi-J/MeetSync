package com.root.meetsync.dto.Notification;

import com.root.meetsync.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private Notification.NotificationType type;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private Long relatedEntityId;
    private String relatedEntityType;
    private String actionUrl;
    
    // Computed fields for UI
    private String timeAgo;
    private String typeLabel;
    private String iconClass;
    private String colorClass;
    
    public static NotificationDTO fromEntity(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationDTO dto = NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .actionUrl(notification.getActionUrl())
                .build();
        
        // Set UI-specific fields
        dto.setTypeLabel(getTypeLabelFromType(notification.getType()));
        dto.setIconClass(getIconClassFromType(notification.getType()));
        dto.setColorClass(getColorClassFromType(notification.getType()));
        
        return dto;
    }
    
    private static String getTypeLabelFromType(Notification.NotificationType type) {
        return switch (type) {
            case EVENT_CREATED -> "Event Created";
            case EVENT_UPDATED -> "Event Updated";
            case EVENT_CANCELLED -> "Event Cancelled";
            case BOOKING_PENDING -> "Booking Pending";
            case BOOKING_CONFIRMED -> "Booking Confirmed";
            case BOOKING_CANCELLED -> "Booking Cancelled";
            case REMINDER -> "Reminder";
            case SYSTEM -> "System";
            case INFO -> "Information";
        };
    }
    
    private static String getIconClassFromType(Notification.NotificationType type) {
        return switch (type) {
            case EVENT_CREATED -> "fas fa-calendar-plus";
            case EVENT_UPDATED -> "fas fa-calendar-edit";
            case EVENT_CANCELLED -> "fas fa-calendar-times";
            case BOOKING_PENDING -> "fas fa-hourglass-half";
            case BOOKING_CONFIRMED -> "fas fa-check-circle";
            case BOOKING_CANCELLED -> "fas fa-times-circle";
            case REMINDER -> "fas fa-bell";
            case SYSTEM -> "fas fa-cog";
            case INFO -> "fas fa-info-circle";
        };
    }
    
    private static String getColorClassFromType(Notification.NotificationType type) {
        return switch (type) {
            case EVENT_CREATED -> "text-green-600";
            case EVENT_UPDATED -> "text-blue-600";
            case EVENT_CANCELLED -> "text-red-600";
            case BOOKING_PENDING -> "text-yellow-600";
            case BOOKING_CONFIRMED -> "text-green-600";
            case BOOKING_CANCELLED -> "text-orange-600";
            case REMINDER -> "text-yellow-600";
            case SYSTEM -> "text-gray-600";
            case INFO -> "text-blue-600";
        };
    }
}
