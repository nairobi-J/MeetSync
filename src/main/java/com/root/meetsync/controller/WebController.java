package com.root.meetsync.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping; // Added this import
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.entity.User;
import com.root.meetsync.service.UserService;
import com.root.meetsync.service.impl.GoogleCalendarServiceImpl;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor 
public class  WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    private final UserService userService; // Added service injection
    private final GoogleCalendarServiceImpl googleCalendarService;

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        if(authentication!= null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupPage(Authentication authentication) {
        if(authentication!= null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "signup";
    }

    @GetMapping("/set-password")
    public String showSetPasswordPage() {
        return "set-password";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        model.addAttribute("timezones", ZoneId.getAvailableZoneIds());
        return "userinfo";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String timezone,
            HttpSession session // Inject Session
    ) throws IOException {

        // 1. Get current cached user to access the ID and old photo URL
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUserDTO");
        if (currentUser == null) {
            return "redirect:/login";
        }

        userService.updateProfile(
                currentUser.getId(),
                name,
                timezone
        );

        // Update session (IMPORTANT)
        currentUser.setName(name);
        currentUser.setTimezone(timezone);

        // Re-save the updated DTO into the session
        session.setAttribute("currentUserDTO", currentUser);

        return "redirect:/profile?success=true";
    }


    @GetMapping("/dashboard")
    public String dashboard(@ModelAttribute("currentUser") CurrentUserDTO currentUser,Authentication authentication, Model model) {
        User user;

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            user = userService.processOAuthUser(oauthToken);
        } else {
            // For manual login, the user must exist.
            String email;
            email = authentication.getName();
            user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Manual user not found"));
        }


        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "redirect:/set-password";
        }

        LocalDate currentDate = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();

        model.addAttribute("currentDate", currentDate);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYear", currentDate.getYear());
        model.addAttribute("activePage", "dash");

        // ── Fetch & convert Google Calendar events ───────────────────────
        List<Map<String, Object>> events = new ArrayList<>();

        try {
            if (user.getOauthToken() != null && user.getOauthToken().getRefreshToken() != null) {
                logger.info("Fetching Google Calendar events for user: {}", user.getEmail());
                List<Event> googleEvents = googleCalendarService.getAllGoogleCalendarEvents(user);
                logger.info("Fetched {} Google events", googleEvents.size());

                events = convertGoogleEventsToCalendarEvents(googleEvents, currentMonth);
                logger.info("Converted {} events for current month", events.size());
            } else {
                logger.info("No Google OAuth token found → no calendar events loaded");
            }
        } catch (Exception e) {
            logger.error("Failed to load Google Calendar events", e);
            // Optionally: model.addAttribute("calendarError", "Could not load calendar events");
        }

        model.addAttribute("events", events);

        
        return "MainHome";
    }
    private List<Map<String, Object>> convertGoogleEventsToCalendarEvents(
            List<Event> googleEvents,
            YearMonth targetMonth) {

        List<Map<String, Object>> result = new ArrayList<>();

        for (Event googleEvent : googleEvents) {
            EventDateTime start = googleEvent.getStart();
            if (start == null) continue;

            LocalDate eventDate = null;

            if (start.getDateTime() != null) {
                eventDate = Instant.ofEpochMilli(start.getDateTime().getValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } else if (start.getDate() != null) {
                eventDate = Instant.ofEpochMilli(start.getDate().getValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            if (eventDate == null) continue;

            // Only include events in the currently viewed month
            if (eventDate.getYear() == targetMonth.getYear() &&
                eventDate.getMonth() == targetMonth.getMonth()) {

                Map<String, Object> event = new HashMap<>();
                event.put("day", eventDate.getDayOfMonth());
                event.put("title", googleEvent.getSummary() != null ? googleEvent.getSummary() : "Untitled");

                String color = "blue";
                String summaryLower = googleEvent.getSummary() != null
                        ? googleEvent.getSummary().toLowerCase()
                        : "";

                if (summaryLower.contains("birthday")) {
                    color = "orange";
                }

                event.put("color", color);
                result.add(event);
            }
        }

        return result;
    }

}