package com.root.meetsync.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.entity.User;
import com.root.meetsync.service.UserService;
import com.root.meetsync.service.impl.GoogleCalendarServiceImpl;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final GoogleCalendarServiceImpl googleCalendarService;
    private final UserService userService;
    
    @RequestMapping("/dashboard")
    
    @GetMapping("/")
    public String home(@ModelAttribute("currentUser") CurrentUserDTO currentUser, Model model) {

        LocalDate currentDate = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        
        model.addAttribute("currentDate", currentDate);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYear", currentDate.getYear());
        
        // Get Google Calendar events
        List<Map<String, Object>> events = new ArrayList<>();
        
        try {
            User user = userService.findByEmail(currentUser.getEmail()).orElse(null);
            
            if (user != null && user.getOauthToken() != null && user.getOauthToken().getRefreshToken() != null) {
                System.out.println("User has Google OAuth token, fetching events...");
                List<Event> googleEvents = googleCalendarService.getAllGoogleCalendarEvents(user);
                System.out.println("Fetched " + googleEvents.size() + " events from Google Calendar");
                events = convertGoogleEventsToCalendarEvents(googleEvents);
                System.out.println("Converted to " + events.size() + " calendar events");
            } else {
                System.out.println("User has no Google Calendar connected, using demo events");
                // User has no Google Calendar connected - use demo events
                events = getDemoEvents();
                System.out.println("Demo events count: " + events.size());
            }
        } catch (Exception e) {
            System.err.println("Error fetching Google Calendar events: " + e.getMessage());
            e.printStackTrace();
            // Fallback to demo events if Google Calendar fails
            events = getDemoEvents();
        }

        System.out.println("Final events count before adding to model: " + events.size());
        model.addAttribute("events", events);
        
        
        return "MainHome";
    }
    
   

    // Convert Google Calendar Events to our calendar format
    private List<Map<String, Object>> convertGoogleEventsToCalendarEvents(List<Event> googleEvents) {
        List<Map<String, Object>> events = new ArrayList<>();
        
        for (Event googleEvent : googleEvents) {
            EventDateTime start = googleEvent.getStart();
            LocalDate eventDate = null;
            
            if (start != null) {
                // Handle events with specific date-time (meetings, appointments)
                if (start.getDateTime() != null) {
                    eventDate = LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(start.getDateTime().getValue()),
                        ZoneId.systemDefault()
                    );
                }
                // Handle all-day events (birthdays, holidays)
                else if (start.getDate() != null) {
                    eventDate = LocalDate.ofInstant(
                        java.time.Instant.ofEpochMilli(start.getDate().getValue()),
                        ZoneId.systemDefault()
                    );
                }
                
                if (eventDate != null) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("day", eventDate.getDayOfMonth());
                    event.put("title", googleEvent.getSummary() != null ? googleEvent.getSummary() : "Untitled");
                    
                    // Color based on event type
                    String color = "blue"; // default
                    if ("birthday".equalsIgnoreCase(googleEvent.getEventType())) {
                        color = "orange";
                    }
                    event.put("color", color);
                    
                    events.add(event);
                }
            }
        }
        
        return events;
    }

    // Demo data method
    private List<Map<String, Object>> getDemoEvents() {
        List<Map<String, Object>> events = new ArrayList<>();
        


     
        // demo events
        events.add(createEvent(1, "Robin", "blue"));
        events.add(createEvent(3, "Quotes", "blue"));
        events.add(createEvent(3, "Giveaway", "orange"));
        events.add(createEvent(5, "Quotes", "blue"));
        events.add(createEvent(5, "Giveaway", "orange"));
        events.add(createEvent(7, "Quotes", "blue"));
        events.add(createEvent(9, "Quotes", "blue"));
        events.add(createEvent(9, "Giveaway", "orange"));
        events.add(createEvent(11, "Quotes", "blue"));
        events.add(createEvent(13, "Quotes", "blue"));
        events.add(createEvent(17, "Quotes", "blue"));
        events.add(createEvent(19, "Quotes", "blue"));
        events.add(createEvent(19, "Giveaway", "orange"));
        events.add(createEvent(19, "Robin", "red"));
        
        return events;
    }
    
    private Map<String, Object> createEvent(int day, String title, String color) {
        Map<String, Object> event = new HashMap<>();
        event.put("day", day);
        event.put("title", title);
        event.put("color", color);
        return event;
    }
}
