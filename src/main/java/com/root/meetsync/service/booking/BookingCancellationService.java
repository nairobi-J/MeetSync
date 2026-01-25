package com.root.meetsync.service.booking;

import com.root.meetsync.entity.User;
import com.root.meetsync.entity.booking.Booking;

import java.util.List;

/**
 * Service for handling booking cancellations due to account deletion
 */
public interface BookingCancellationService {
    
    /**
     * Cancel all active bookings for a deleted user and notify invitees
     * @param deletedUser The deleted user
     * @param reason Reason for cancellation (e.g., "Account deleted")
     * @return List of cancelled bookings
     */
    List<Booking> cancelAllUserBookings(User deletedUser, String reason);
    
    /**
     * Send cancellation notification to a booking invitee
     * @param booking The cancelled booking
     * @param reason Reason for cancellation
     */
    void notifyInviteeCancellation(Booking booking, String reason);
    
    /**
     * Clean up Google Calendar events for cancelled bookings
     * @param deletedUser The deleted user
     * @param bookings List of cancelled bookings
     * @return Number of successfully cleaned up calendar events
     */
    int cleanupBookingGoogleCalendarEvents(User deletedUser, List<Booking> bookings);
}
