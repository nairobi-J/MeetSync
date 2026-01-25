package com.root.meetsync.service.booking.impl;

import com.root.meetsync.entity.User;
import com.root.meetsync.entity.booking.Booking;
import com.root.meetsync.entity.booking.BookingStatus;
import com.root.meetsync.repository.booking.BookingRepository;
import com.root.meetsync.service.booking.BookingCancellationService;
import com.root.meetsync.service.impl.GoogleCalendarServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class BookingCancellationServiceImpl implements BookingCancellationService {

    private final BookingRepository bookingRepository;
    private final GoogleCalendarServiceImpl googleCalendarService;

    public BookingCancellationServiceImpl(
            BookingRepository bookingRepository,
            GoogleCalendarServiceImpl googleCalendarService) {
        this.bookingRepository = bookingRepository;
        this.googleCalendarService = googleCalendarService;
    }

    @Override
    public List<Booking> cancelAllUserBookings(User deletedUser, String reason) {
        System.out.println("Starting booking cancellation for user: " + deletedUser.getEmail() + " (ID: " + deletedUser.getId() + ")");
        
        // Find all active bookings for the deleted user
        List<Booking> activeBookings = bookingRepository.findActiveBookingsByHost(deletedUser);
        
        System.out.println("Found " + activeBookings.size() + " active bookings to cancel for user: " + deletedUser.getEmail());
        
        for (Booking booking : activeBookings) {
            try {
                System.out.println("Cancelling booking ID " + booking.getId() + " - Status: " + booking.getStatus() + ", Invitee: " + booking.getInviteeEmail());
                
                // Cancel the booking
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                
                // Notify the invitee
                notifyInviteeCancellation(booking, reason);
                
                System.out.println("Successfully cancelled booking ID " + booking.getId() + " for invitee: " + booking.getInviteeEmail());
                
            } catch (Exception e) {
                System.err.println("Failed to cancel booking ID " + booking.getId() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return activeBookings;
    }

    @Override
    public void notifyInviteeCancellation(Booking booking, String reason) {
        // TODO: Implement email notification when mail dependencies are available
        // For now, we'll just log the cancellation
        try {
            String message = buildCancellationMessage(booking, reason);
            System.out.println("BOOKING CANCELLATION NOTIFICATION:");
            System.out.println("To: " + booking.getInviteeEmail());
            System.out.println("Subject: Meeting Cancelled - " + formatBookingTitle(booking));
            System.out.println("Message: " + message);
            System.out.println("Cancellation notification logged for: " + booking.getInviteeEmail());
            
        } catch (Exception e) {
            System.err.println("Failed to log cancellation notification for " + booking.getInviteeEmail() + ": " + e.getMessage());
        }
    }

    @Override
    public int cleanupBookingGoogleCalendarEvents(User deletedUser, List<Booking> bookings) {
        int successCount = 0;
        
        for (Booking booking : bookings) {
            if (booking.getGoogleCalendarEventId() != null && !booking.getGoogleCalendarEventId().isEmpty()) {
                try {
                    boolean deleted = googleCalendarService.deleteGoogleEvent(deletedUser, booking.getGoogleCalendarEventId());
                    
                    if (deleted) {
                        // Clear the Google Calendar ID from the booking
                        booking.setGoogleCalendarEventId(null);
                        bookingRepository.save(booking);
                        successCount++;
                        
                        System.out.println("Successfully deleted Google Calendar event for booking ID: " + booking.getId());
                    } else {
                        System.out.println("Failed to delete Google Calendar event for booking ID: " + booking.getId());
                    }
                    
                } catch (Exception e) {
                    System.err.println("Error deleting Google Calendar event for booking ID " + booking.getId() + ": " + e.getMessage());
                }
            }
        }
        
        System.out.println("Successfully cleaned up " + successCount + " Google Calendar events for bookings");
        return successCount;
    }

    private String formatBookingTitle(Booking booking) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        return "Meeting on " + booking.getStartTime().format(formatter);
    }

    private String buildCancellationMessage(Booking booking, String reason) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(booking.getInviteeName()).append(",\n\n");
        body.append("We regret to inform you that your scheduled meeting has been cancelled.\n\n");
        body.append("Meeting Details:\n");
        body.append("Date: ").append(booking.getStartTime().format(dateFormatter)).append("\n");
        body.append("Time: ").append(booking.getStartTime().format(timeFormatter))
            .append(" - ").append(booking.getEndTime().format(timeFormatter));
        
        if (booking.getTimezone() != null) {
            body.append(" (").append(booking.getTimezone()).append(")");
        }
        body.append("\n");
        body.append("Reason: ").append(reason).append("\n\n");
        
        body.append("If you have any questions or would like to reschedule, please contact us at support@meetsync.com\n\n");
        body.append("We apologize for any inconvenience this may cause.\n\n");
        body.append("Best regards,\n");
        body.append("The MeetSync Team");
        
        return body.toString();
    }
}
