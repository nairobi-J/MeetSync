package com.root.meetsync.controller.booking;

import com.root.meetsync.dto.availability.AvailableSlotDTO;
import com.root.meetsync.dto.booking.BookingRequestDTO;
import com.root.meetsync.dto.booking.BookingResponseDTO;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.availability.UserMeetingPreference;
import com.root.meetsync.service.UserService;
import com.root.meetsync.service.availability.IAvailabilityService;
import com.root.meetsync.service.booking.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookingPageController {

    private final IAvailabilityService availabilityService;
    private final UserService userService;
    private final IBookingService bookingService;

    @GetMapping("/u/{emailPrefix}")
    public String showPublicBookingPage(@PathVariable String emailPrefix,
            @RequestParam(required = false) String timezone, Model model) {

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
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        return "availability/booking";
    }

    @PostMapping("/u/{emailPrefix}")
    public String createBooking(
            @PathVariable String emailPrefix,
            RedirectAttributes redirectAttributes,
            @ModelAttribute BookingRequestDTO request
           ) {
        try {
            bookingService.createBookingRequest(emailPrefix, request);
            
            redirectAttributes.addFlashAttribute("success", true);
            redirectAttributes.addFlashAttribute("message", "Scheduling request sent to Host! Check your email for confirmation.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", true);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }
        
        return "redirect:/u/" + emailPrefix;
    }
}
