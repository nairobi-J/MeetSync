package com.root.meetsync.controller.availability;

import com.root.meetsync.dto.availability.SetupAvailabilityRequest;
import com.root.meetsync.entity.User;
import com.root.meetsync.service.availability.IAvailabilityService;
import com.root.meetsync.service.UserService;
import lombok.RequiredArgsConstructor;
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
public class AvailabilityWebController {

    private final IAvailabilityService availabilityService;
    private final UserService userService;

    @GetMapping("/setup")
    public String showAvailabilitySetup(Authentication authentication, Model model) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = getAuthenticatedUser(authentication);


        SetupAvailabilityRequest request = new SetupAvailabilityRequest();
        model.addAttribute("availabilityRequest", request);
        model.addAttribute("user", user);
        
        // days of week for form
        model.addAttribute("daysOfWeek", DayOfWeek.values());
        
        // Tmezone options
        model.addAttribute("timezones", new String[]{
            "GMT+6 BDT", "GMT+5:30 IST", "GMT+0 UTC", 
            "GMT-5 EST", "GMT-8 PST", "GMT+1 CET"
        });
        
        // Duration options (in minutes)
        model.addAttribute("durations", new Integer[]{15, 30, 45, 60, 90, 120});
        
        return "availability/setup";
    }

    @PostMapping("/setup")
    public String setupAvailability(
            Authentication authentication,
            @ModelAttribute SetupAvailabilityRequest request,
            RedirectAttributes redirectAttributes) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = getAuthenticatedUser(authentication);

        try {
            availabilityService.setupAvailability(user.getId(), request);
            redirectAttributes.addFlashAttribute("success", "Availability setup successfully!");
            
         
            String bookingLink = availabilityService.getUserBookingLink(user.getId());
            redirectAttributes.addFlashAttribute("bookingLink", bookingLink);
            
            return "redirect:/availability/success";
        } catch (Exception e) {
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
            return "redirect:/login";
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
            user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Manual user not found"));
        }

        return user;
    }
}
