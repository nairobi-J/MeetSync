package com.root.meetsync.controller.availability;

import com.root.meetsync.dto.availability.SetupAvailabilityRequest;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.Notification.NotificationType;
import com.root.meetsync.service.availability.IAvailabilityService;
import com.root.meetsync.service.NotificationService;
import com.root.meetsync.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.weaver.ast.Not;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;

@Controller
@RequestMapping("/availability")
@RequiredArgsConstructor
@Slf4j
public class AvailabilityPageController {

    private final IAvailabilityService availabilityService;
    private final UserService userService;
    private final NotificationService notificationService;

    @GetMapping("/setup")
    public String showAvailabilitySetup(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/oauth2/authorization/google";
        }

        User user = getAuthenticatedUser(authentication);

        SetupAvailabilityRequest request;

        // Try to fetch existing availability data
        try {
            request = availabilityService.getUserAvailability(user.getId());

            if (request.getWeeklyAvailability() != null && !request.getWeeklyAvailability().isEmpty()) {
                log.info("Loaded existing availability for user {} with {} weekly slots", user.getId(),
                        request.getWeeklyAvailability().size());
                model.addAttribute("hasExistingAvailability", true);
            } else {
                model.addAttribute("hasExistingAvailability", false);
            }
        } catch (Exception e) {
            log.debug("No existing availability found for user {}, using defaults", user.getId());
            request = new SetupAvailabilityRequest();
            model.addAttribute("hasExistingAvailability", false);
        }

        model.addAttribute("availabilityRequest", request);
        model.addAttribute("user", user);
        model.addAttribute("isisAvailable", true);

        model.addAttribute("activePage", "availability");
        // Add booking link to the model
        try {
            String bookingLink = availabilityService.getUserBookingLink(user.getId());
            model.addAttribute("bookingLink", bookingLink);
        } catch (Exception e) {
            log.debug("Could not retrieve booking link for user {}", user.getId());
        }

        // days of week for form
        String[] daysOfWeek = { "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" };
       
        model.addAttribute("daysOfWeek", daysOfWeek);

        // Timezone options
        model.addAttribute("timezones", new String[] { "Asia/Dhaka", "Asia/Kolkata", "UTC", "America/New_York",
                "America/Los_Angeles", "Europe/Paris", "Asia/Tokyo", "Australia/Sydney" });

        // Duration options (in minutes)
        model.addAttribute("durations", new Integer[] { 15, 30, 45, 60, 90, 120 });

        return "availability/setup";
    }

    @PostMapping("/setup")
    public String setupAvailability(Authentication authentication, @ModelAttribute SetupAvailabilityRequest request,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/oauth2/authorization/google";
        }

        User user = getAuthenticatedUser(authentication);

        try {

            availabilityService.setupAvailability(user.getId(), request);
            redirectAttributes.addFlashAttribute("success", "Availability saved successfully!");

            String bookingLink = availabilityService.getUserBookingLink(user.getId());
            redirectAttributes.addFlashAttribute("bookingLink", bookingLink);
            redirectAttributes.addFlashAttribute("isAvailable", true);

            notificationService.createNotification(user, "Availability Updated",
                    "Your availability has been updated successfully.", NotificationType.SYSTEM, null, // relatedEntityId,
                                                                                                       // if any
                    "Availability", "/availability/setup");

            return "redirect:/availability/setup";
        } catch (Exception e) {
            log.error("Failed to setup availability for user {}", user.getId(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to setup availability: " + e.getMessage());
            return "redirect:/availability/setup";
        }
    }

    @GetMapping("/success")
    public String showSuccess() {
        return "availability/success";
    }

    @GetMapping("/link")
    public String showBookingLink(Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/oauth2/authorization/google";
        }

        User user = getAuthenticatedUser(authentication);

        String bookingLink = availabilityService.getUserBookingLink(user.getId());
        model.addAttribute("bookingLink", bookingLink);
        model.addAttribute("user", user);

        return "availability/link";
    }

    private User getAuthenticatedUser(Authentication authentication) {
        User user;

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            user = userService.processOAuthUser(oauthToken);
        } else {

            String email = authentication.getName();
            user = userService.findByEmail(email).orElseThrow(() -> new RuntimeException("Manual user not found"));
        }

        return user;
    }
}
