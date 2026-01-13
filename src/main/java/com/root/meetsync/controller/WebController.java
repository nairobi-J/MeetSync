package com.root.meetsync.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    private final UserService userService;
    private final GoogleCalendarServiceImpl googleCalendarService;

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
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
            HttpSession session) throws IOException {

        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUserDTO");
        if (currentUser == null) {
            return "redirect:/login";
        }

        userService.updateProfile(currentUser.getId(), name, timezone);

        currentUser.setName(name);
        currentUser.setTimezone(timezone);
        session.setAttribute("currentUserDTO", currentUser);

        return "redirect:/profile?success=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @ModelAttribute("currentUser") CurrentUserDTO currentUser,
            Authentication authentication,
            Model model) {

        User user = getAuthenticatedUser(authentication);

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "redirect:/set-password";
        }

        LocalDate currentDate = LocalDate.now();
        YearMonth currentMonthObj = YearMonth.now();
        int displayMonth = currentMonthObj.getMonthValue();
        int displayYear = currentDate.getYear();

        model.addAttribute("currentDate", currentDate);
        model.addAttribute("currentMonth", currentMonthObj);
        model.addAttribute("currentYear", displayYear);
        model.addAttribute("activePage", "dash");

        List<Map<String, Object>> events = getEventsForMonth(user, displayMonth, displayYear);
        model.addAttribute("events", events);

        return "MainHome";
    }

    @GetMapping("/api/events")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getEventsApi(
            Authentication authentication,
            @RequestParam int month,
            @RequestParam int year) {

        User user = getAuthenticatedUser(authentication);
        List<Map<String, Object>> events = getEventsForMonth(user, month, year);
        return ResponseEntity.ok(events);
    }

    private List<Map<String, Object>> getEventsForMonth(User user, int targetMonth, int targetYear) {
        List<Map<String, Object>> events = new ArrayList<>();

        try {
            if (user.getOauthToken() != null && user.getOauthToken().getRefreshToken() != null) {
                logger.info("Fetching events for {} - month: {}/{}", user.getEmail(), targetMonth, targetYear);
                List<Event> googleEvents = googleCalendarService.getAllGoogleCalendarEvents(user);
                logger.info("Fetched {} Google events", googleEvents.size());

                events = convertGoogleEventsToCalendarEvents(googleEvents, targetMonth, targetYear);
                logger.info("Converted {} events for {}/{}", events.size(), targetMonth, targetYear);
            }
        } catch (Exception e) {
            logger.error("Failed to load calendar events", e);
        }

        return events;
    }

    private List<Map<String, Object>> convertGoogleEventsToCalendarEvents(
            List<Event> googleEvents, int targetMonth, int targetYear) {

        List<Map<String, Object>> result = new ArrayList<>();

        for (Event e : googleEvents) {
            EventDateTime start = e.getStart();
            if (start == null) continue;

            LocalDate eventDate = null;
            if (start.getDateTime() != null) {
                eventDate = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(start.getDateTime().getValue()), ZoneId.systemDefault());
            } else if (start.getDate() != null) {
                eventDate = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(start.getDate().getValue()), ZoneId.systemDefault());
            }

            if (eventDate == null) continue;

            if (eventDate.getMonthValue() == targetMonth && eventDate.getYear() == targetYear) {
                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("date", eventDate.toString());
                eventMap.put("day", eventDate.getDayOfMonth());
                eventMap.put("title", e.getSummary() != null ? e.getSummary() : "Untitled");
                eventMap.put("description", e.getDescription() != null ? e.getDescription() : "");
                String color = "blue";
                String summary = e.getSummary() != null ? e.getSummary().toLowerCase() : "";
                if (summary.contains("birthday")) color = "orange";
                eventMap.put("color", color);
                result.add(eventMap);
            }
        }
        return result;
    }

    private User getAuthenticatedUser(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken token) {
            return userService.processOAuthUser(token);
        }
        String email = auth.getName();
        return userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}