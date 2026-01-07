package com.root.meetsync.controller.booking;

import com.root.meetsync.dto.availability.AvailableSlotDTO;
import com.root.meetsync.service.availability.IAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookingPageController {

    private final IAvailabilityService availabilityService;

    @GetMapping("/u/{emailPrefix}")
    public String showPublicBookingPage(
            @PathVariable String emailPrefix,
            @RequestParam(required = false) String timezone,
            Model model) {
        try {
            // Fetch available slots
            List<AvailableSlotDTO> slots = availabilityService.getAvailableSlots(emailPrefix, timezone);
            
            model.addAttribute("emailPrefix", emailPrefix);
            model.addAttribute("availableSlots", slots);
            model.addAttribute("timezone", timezone != null ? timezone : "UTC");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        
        return "availability/booking";
    }
}
