package com.root.meetsync.controller.booking;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.dto.availability.AvailableSlotDTO;
import com.root.meetsync.dto.booking.BookingRequestDTO;
import com.root.meetsync.dto.booking.BookingResponseDTO;
import com.root.meetsync.entity.Notification;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.availability.UserMeetingPreference;
import com.root.meetsync.service.NotificationService;
import com.root.meetsync.service.UserService;
import com.root.meetsync.service.availability.IAvailabilityService;
import com.root.meetsync.service.booking.IBookingService;
import com.root.meetsync.service.impl.GoogleCalendarServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequiredArgsConstructor

public class BookingPageController {

    private final IAvailabilityService availabilityService;
    private final UserService userService;
    private final IBookingService bookingService;
    private final NotificationService notificationService;
    private final GoogleCalendarServiceImpl googleCalendarServiceImpl;

    @GetMapping("/u/{emailPrefix}")
    public String showPublicBookingPage(@PathVariable String emailPrefix,
            @RequestParam(required = false) String timezone, Model model,@ModelAttribute(value="currentUser" ,binding = false) CurrentUserDTO currentUser) {

        try {
            // Fetch available slots
            List<AvailableSlotDTO> slots = availabilityService.getAvailableSlots(emailPrefix, timezone);
            User user = userService.getUserByEmailPrefix(emailPrefix);
            UserMeetingPreference preference = availabilityService.getUserMeetingPreference(user.getId());

            model.addAttribute("Preferences", preference);

            model.addAttribute("user", user);

            model.addAttribute("emailPrefix", emailPrefix);
            model.addAttribute("availableSlots", slots);
            model.addAttribute("timezone", timezone != null ? timezone : "UTC");
           
            boolean isOwner = currentUser != null && currentUser.getEmail().equalsIgnoreCase(user.getEmail());
            model.addAttribute("isOwner", isOwner);

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "bookings/booking";
    }

    @PostMapping("/u/{emailPrefix}")
    public String createBooking(@PathVariable String emailPrefix, RedirectAttributes redirectAttributes,
            @ModelAttribute BookingRequestDTO request) {
        try {
            BookingResponseDTO booking = bookingService.createBookingRequest(emailPrefix, request);

            User host = userService.getUserByEmailPrefix(emailPrefix);
            if (host != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
                String bookingTime = request.getStartTime().format(formatter);

                notificationService.createNotification(host, "New Appointment Request",
                        request.getInviteeName() + " (" + request.getInviteeEmail() + ") has requested a meeting on "
                                + bookingTime,
                        Notification.NotificationType.BOOKING_PENDING, booking.getId(), "Booking", "/dashboard");
            }

            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message",
                    "Scheduling request sent to Host! Check your email for confirmation.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/u/" + emailPrefix;
    }

    @PutMapping("/{bookingId}/confirm/{notificationId}")
    public String confirmBooking(@PathVariable Long bookingId, @PathVariable Long notificationId,
            RedirectAttributes redirectAttributes) {
        try {
            BookingResponseDTO response = bookingService.confirmBooking(bookingId);
            String prefix = response.getHostEmail().split("@")[0];
            User host = userService.getUserByEmailPrefix(prefix);

            // 3. AUTO-TRIGGER GOOGLE CALENDAR AND STORE EVENT ID
            try {
                String googleEventId = googleCalendarServiceImpl.createGoogleEvent(host, response);
                if (googleEventId != null) {
                    // Update booking with Google Calendar event ID
                    bookingService.updateGoogleCalendarEventId(bookingId, googleEventId);
                    System.out.println("Successfully synced booking to Google Calendar: " + googleEventId);
                } else {
                    System.err.println("Google Calendar Sync Failed: No event ID returned");
                    bookingService.markGoogleCalendarSyncFailed(bookingId);
                }
            } catch (Exception e) {
                System.err.println("Google Calendar Sync Failed: " + e.getMessage());
                bookingService.markGoogleCalendarSyncFailed(bookingId);
                // We don't throw an error here so the DB confirmation still finishes
            }

            // delete pending notification
            notificationService.deleteNotification(notificationId);

            if (response != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
                String bookingTime = response.getStartTime().format(formatter);

                notificationService.createNotification(host, "Schedule Confirmed",
                        "You have confirmed the booking with " + response.getInviteeName() + " on " + bookingTime,
                        Notification.NotificationType.BOOKING_CONFIRMED, response.getId(), "Booking", "/bookings");
            }
            redirectAttributes.addFlashAttribute("success",
                    "Schedule confirmed successfully & synced to Google Calendar!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/notifications";
    }

    @PutMapping("/{bookingId}/cancel/{notificationId}")
    public String cancelBooking(@PathVariable Long bookingId, @PathVariable Long notificationId,
            RedirectAttributes redirectAttributes) {
        try {
            BookingResponseDTO response = bookingService.cancelBooking(bookingId);

            if (response != null) {
                String prefix = response.getHostEmail().split("@")[0];
                User host = userService.getUserByEmailPrefix(prefix);

                // Delete the event from Google Calendar if it exists
                if (response.getGoogleCalendarEventId() != null) {
                    try {
                        googleCalendarServiceImpl.deleteGoogleEvent(host, response.getGoogleCalendarEventId());
                        System.out.println("Successfully deleted booking from Google Calendar: "
                                + response.getGoogleCalendarEventId());
                    } catch (Exception e) {
                        System.err.println("Failed to delete Google Calendar event: " + e.getMessage());
                        // Continue with the cancellation even if Google Calendar deletion fails
                    }
                }

                // delete pending notification
                notificationService.deleteNotification(notificationId);

                notificationService.createNotification(host, "Schedule Cancelled",
                        "The booking with " + response.getInviteeName() + " has been cancelled",
                        Notification.NotificationType.BOOKING_CANCELLED, response.getId(), "Booking", "/bookings");
            }
            redirectAttributes.addFlashAttribute("success", "Schedule cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/notifications";
    }
}
