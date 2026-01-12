package com.root.meetsync.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.calendar.model.Event;
import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.entity.User;
import com.root.meetsync.service.UserService;
import com.root.meetsync.service.impl.GoogleCalendarServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/test/calendar")
@RequiredArgsConstructor
@Slf4j
public class TestCalendarController {

    private final GoogleCalendarServiceImpl googleCalendarService;
    private final UserService userService;

    @GetMapping("/events")
    public ResponseEntity<?> getAllGoogleEvents(@ModelAttribute("currentUser") CurrentUserDTO currentUser) {
        try {
            log.info("=== TEST: Fetching Google Calendar Events ===");
            log.info("Current User Email: {}", currentUser.getEmail());

            User user = userService.findByEmail(currentUser.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found: " + currentUser.getEmail()));

            log.info("User found in DB: {}", user.getEmail());
            log.info("Has OAuth Token: {}", user.getOauthToken() != null);
            
            if (user.getOauthToken() != null) {
                log.info("Has Refresh Token: {}", user.getOauthToken().getRefreshToken() != null);
            }

            List<Event> googleEvents = googleCalendarService.getAllGoogleCalendarEvents(user);
            log.info("Total events fetched from Google Calendar: {}", googleEvents.size());

            // Convert to simple format for easy viewing
            List<String> eventSummaries = googleEvents.stream()
                    .map(event -> {
                        String summary = event.getSummary() != null ? event.getSummary() : "No Title";
                        String start = event.getStart() != null && event.getStart().getDateTime() != null 
                                ? event.getStart().getDateTime().toString() 
                                : "No Start Time";
                        return summary + " - " + start;
                    })
                    .collect(Collectors.toList());

            log.info("Events: {}", eventSummaries);

            return ResponseEntity.ok(new TestResponse(
                    true,
                    "Successfully fetched " + googleEvents.size() + " events",
                    googleEvents.size(),
                    eventSummaries,
                    googleEvents
            ));

        } catch (Exception e) {
            log.error("ERROR fetching Google Calendar events", e);
            return ResponseEntity.status(500).body(new ErrorResponse(
                    false,
                    "Error: " + e.getMessage(),
                    e.getClass().getSimpleName()
            ));
        }
    }

    // Response DTOs
    record TestResponse(
            boolean success,
            String message,
            int eventCount,
            List<String> eventSummaries,
            List<Event> fullEvents
    ) {}

    record ErrorResponse(
            boolean success,
            String error,
            String errorType
    ) {}
}
