package com.root.meetsync.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_deletions")
public class UserDeletion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String originalEmail;
    
    @Column(nullable = false)
    private LocalDateTime deletedAt;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DeletionReason reason;
    
    @Column(nullable = false)
    private Long deletedBy; // User ID who initiated the deletion
    
    @Column(length = 500)
    private String notes; // Additional notes or reason details
    
    @Column(nullable = false)
    private Boolean googleCalendarCleanupAttempted = false;
    
    @Column(nullable = false)
    private Boolean googleCalendarCleanupSuccessful = false;
    
    // Default constructor
    public UserDeletion() {}
    
    // Constructor for self-deletion
    public UserDeletion(User user, DeletionReason reason, String notes) {
        this.userId = user.getId();
        this.originalEmail = user.getEmail();
        this.deletedAt = LocalDateTime.now();
        this.reason = reason;
        this.deletedBy = user.getId(); // Self-deletion
        this.notes = notes;
    }
    
    // Constructor for admin deletion
    public UserDeletion(User user, User admin, DeletionReason reason, String notes) {
        this.userId = user.getId();
        this.originalEmail = user.getEmail();
        this.deletedAt = LocalDateTime.now();
        this.reason = reason;
        this.deletedBy = admin.getId();
        this.notes = notes;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getOriginalEmail() {
        return originalEmail;
    }
    
    public void setOriginalEmail(String originalEmail) {
        this.originalEmail = originalEmail;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public DeletionReason getReason() {
        return reason;
    }
    
    public void setReason(DeletionReason reason) {
        this.reason = reason;
    }
    
    public Long getDeletedBy() {
        return deletedBy;
    }
    
    public void setDeletedBy(Long deletedBy) {
        this.deletedBy = deletedBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Boolean getGoogleCalendarCleanupAttempted() {
        return googleCalendarCleanupAttempted;
    }
    
    public void setGoogleCalendarCleanupAttempted(Boolean googleCalendarCleanupAttempted) {
        this.googleCalendarCleanupAttempted = googleCalendarCleanupAttempted;
    }
    
    public Boolean getGoogleCalendarCleanupSuccessful() {
        return googleCalendarCleanupSuccessful;
    }
    
    public void setGoogleCalendarCleanupSuccessful(Boolean googleCalendarCleanupSuccessful) {
        this.googleCalendarCleanupSuccessful = googleCalendarCleanupSuccessful;
    }
}
