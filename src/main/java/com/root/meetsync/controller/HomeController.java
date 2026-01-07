package com.root.meetsync.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller

public class HomeController {
    
    @RequestMapping("/dashboard")
    
    @GetMapping("/")
    public String home(Model model) {

        LocalDate currentDate = LocalDate.now();
        YearMonth currentMonth = YearMonth.now();
        
        model.addAttribute("currentDate", currentDate);
        model.addAttribute("currentMonth", currentMonth);
        model.addAttribute("currentYear", currentDate.getYear());
        
        // Demo calendar events
        List<Map<String, Object>> events = getDemoEvents();
        model.addAttribute("events", events);
        
        
        return "MainHome";
    }
    
    @GetMapping("/schedules")
    public String schedules(Model model) {
        return "schedules";
    }
    
    @GetMapping("/availability")
    public String availability(Model model) {
        return "availability";
    }
    
    @GetMapping("/settings")
    public String settings(Model model) {
        return "settings";
    }
    
    // @GetMapping("/notifications")
    // public String notifications(Model model) {
    //     return "notifications";
    // }
    
    // Demo data method
    private List<Map<String, Object>> getDemoEvents() {
        List<Map<String, Object>> events = new ArrayList<>();
        
        // demo events
        events.add(createEvent(1, "Quotes", "blue"));
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
        events.add(createEvent(19, "Reel", "red"));
        
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
