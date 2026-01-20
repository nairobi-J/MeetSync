package com.root.meetsync.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
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
import com.root.meetsync.entity.UserStatus;
import com.root.meetsync.service.UserService;
import com.root.meetsync.service.impl.GoogleCalendarServiceImpl;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    private final UserService userService;
    private final GoogleCalendarServiceImpl googleCalendarService;

    // @GetMapping("/login")
    // public String showLoginPage(Authentication authentication) {

    //     if (authentication != null && authentication.isAuthenticated()) {
    //         return "redirect:/";
    //     }
    //     return "login";
    // }

    // @GetMapping("/signup")
    // public String showSignupPage(Authentication authentication) {
    //     if (authentication != null && authentication.isAuthenticated()) {
    //         return "redirect:/";
    //     }
    //     return "signup";
    // }

    // @GetMapping("/set-password")
    // public String showSetPasswordPage() {
    //     return "set-password";
    // }

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        model.addAttribute("timezones", ZoneId.getAvailableZoneIds());
        model.addAttribute("activePage", "profile");
        return "userinfo";
    }

    @GetMapping("/pending-approval")
    public String pendingApprovalPage() {
        return "pending-approval";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String timezone,
            HttpSession session) throws IOException {

        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUserDTO");
        if (currentUser == null) {
            return "redirect:/oauth2/authorization/google";
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
        Model model,
        @RequestParam(name = "year", required = false) Integer paramYear,
        @RequestParam(name = "month", required = false) Integer paramMonth,
        HttpSession session) {

    User user = getAuthenticatedUser(authentication);

    // if (user.getPassword() == null || user.getPassword().isEmpty()) {
    //     return "redirect:/set-password";
    // }

    LocalDate now = LocalDate.now();
    int displayYear  = (paramYear != null) ? paramYear : now.getYear();
    int displayMonth = (paramMonth != null && paramMonth >= 1 && paramMonth <= 12) ? paramMonth : now.getMonthValue();

    YearMonth ym = YearMonth.of(displayYear, displayMonth);

    model.addAttribute("currentDate", now);
    model.addAttribute("currentMonth", ym);
    model.addAttribute("currentYear", displayYear);
    model.addAttribute("activePage", "dash");

    // Initial events: full year (client will use them and fetch more years if needed)
    List<Map<String, Object>> events = getEventsForYear(user, displayYear);
    model.addAttribute("events", events);

    return "MainHome";
    }

    @GetMapping("/api/events/year")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getEventsForYearApi(
            Authentication authentication,
            @RequestParam int year) {

        User user = getAuthenticatedUser(authentication);
        List<Map<String, Object>> events = getEventsForYear(user, year);
        return ResponseEntity.ok(events);
    }





    private List<Map<String, Object>> getEventsForYear(User user, int targetYear) {
    List<Map<String, Object>> events = new ArrayList<>();

    try {
        if (user.getOauthToken() != null && user.getOauthToken().getRefreshToken() != null) {
            logger.info("Fetching full year events for {} - year: {}", user.getEmail(), targetYear);
            List<Event> googleEvents = googleCalendarService.getAllGoogleCalendarEvents(user);
            logger.info("Fetched {} Google events", googleEvents.size());

            events = convertGoogleEventsToCalendarEventsForYear(googleEvents, targetYear);
            logger.info("Converted {} events for year {}", events.size(), targetYear);
        }
    } catch (Exception e) {
        logger.error("Failed to load calendar events for year " + targetYear, e);
    }

    return events;
    }

    private List<Map<String, Object>> convertGoogleEventsToCalendarEventsForYear(
        List<Event> googleEvents, int targetYear) {

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

        if (eventDate == null || eventDate.getYear() != targetYear) continue;

        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("date", eventDate.toString());
        eventMap.put("month", eventDate.getMonthValue());
        eventMap.put("day", eventDate.getDayOfMonth());
        eventMap.put("title", e.getSummary() != null ? e.getSummary() : "Untitled");
        eventMap.put("description", e.getDescription() != null ? e.getDescription() : "");
        
        // Extract start time
        if (start.getDateTime() != null) {
            java.time.Instant startInstant = java.time.Instant.ofEpochMilli(start.getDateTime().getValue());
            java.time.LocalTime startTime = java.time.LocalTime.ofInstant(startInstant, ZoneId.systemDefault());
            eventMap.put("startTime", startTime.toString());
        } else if (start.getDate() != null) {
            eventMap.put("startTime", "All day");
        }
        
        // Extract end time
        EventDateTime end = e.getEnd();
        if (end != null) {
            if (end.getDateTime() != null) {
                java.time.Instant endInstant = java.time.Instant.ofEpochMilli(end.getDateTime().getValue());
                java.time.LocalTime endTime = java.time.LocalTime.ofInstant(endInstant, ZoneId.systemDefault());
                eventMap.put("endTime", endTime.toString());
            } else if (end.getDate() != null) {
                eventMap.put("endTime", "All day");
            }
        }
        
        String color = "blue";
        String summary = e.getSummary() != null ? e.getSummary().toLowerCase() : "";
        if (summary.contains("birthday")) color = "orange";
        eventMap.put("color", color);
        result.add(eventMap);
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