package com.root.meetsync.service;

import com.root.meetsync.entity.DeletionReason;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserDeletion;

public interface AccountDeletionService {
    
    /**
     * Soft delete a user account (self-deletion)
     * @param user The user to delete
     * @param reason The reason for deletion
     * @param notes Additional notes from the user
     * @return UserDeletion record
     */
    UserDeletion deleteUserAccount(User user, DeletionReason reason, String notes);
    
    /**
     * Admin-initiated soft delete of a user account
     * @param user The user to delete
     * @param admin The admin performing the deletion
     * @param reason The reason for deletion
     * @param notes Additional notes from the admin
     * @return UserDeletion record
     */
    UserDeletion deleteUserAccountByAdmin(User user, User admin, DeletionReason reason, String notes);
    
    /**
     * Anonymize user data after deletion
     * @param user The user whose data should be anonymized
     */
    void anonymizeUserData(User user);
    
    /**
     * Attempt to clean up Google Calendar events for deleted user
     * @param deletion The deletion record
     * @return true if cleanup was successful, false otherwise
     */
    boolean attemptGoogleCalendarCleanup(UserDeletion deletion);
    
    /**
     * Check if a user is deleted
     * @param userId The user ID to check
     * @return true if user is deleted, false otherwise
     */
    boolean isUserDeleted(Long userId);
    
    /**
     * Get deletion record for a user
     * @param userId The user ID
     * @return UserDeletion record if exists
     */
    UserDeletion getDeletionRecord(Long userId);
    
    /**
     * Cancel all active bookings for a deleted user
     * @param user The deleted user
     * @return Number of bookings cancelled
     */
    int cancelUserBookings(User user);
    
    /**
     * Clean up Google Calendar events for cancelled bookings
     * @param user The deleted user
     * @return true if cleanup was successful, false otherwise
     */
    boolean cleanupBookingCalendarEvents(User user);
}
