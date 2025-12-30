package com.root.meetsync.controller;


import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.root.meetsync.dto.EventForm;
import com.root.meetsync.service.impl.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ViewController {

    @Autowired
    private GoogleCalendarService calendarService;

    // Show Today's Events
    @GetMapping("/myevents-today")
    public String getMyEventsToday(Model model) {
        System.out.println("=== myevents-today endpoint hit ===");

        try {
            Events events = calendarService.getTodayEvents();

            if (events != null) {
                model.addAttribute("calendarSummary", events.getSummary());
                model.addAttribute("timeZone", events.getTimeZone());

                List<Event> eventsList = events.getItems();
                if (eventsList != null && !eventsList.isEmpty()) {
                    model.addAttribute("events", eventsList);
                    model.addAttribute("hasEvents", true);
                    model.addAttribute("eventCount", eventsList.size());
                } else {
                    model.addAttribute("hasEvents", false);
                    model.addAttribute("eventCount", 0);
                }
            } else {
                model.addAttribute("hasEvents", false);
                model.addAttribute("eventCount", 0);
            }

            System.out.println("Returning template: meeting/myevents-today");
            return "meeting/myevents-today";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to load events: " + e.getMessage());
            model.addAttribute("hasEvents", false);
            return "meeting/myevents-today";
        }
    }


    // Show Add Event Form
    @GetMapping("/events/add")
    public String showAddEventForm(Model model) {
        model.addAttribute("eventForm", new EventForm());
        return "meeting/add-event";
    }


    // Handle Add Event Submission
    @PostMapping("/events/add")
    public String addEvent(
            @ModelAttribute EventForm eventForm,
            RedirectAttributes redirectAttributes) {

        try {
            System.out.println("Adding event: " + eventForm);

            Event event = calendarService.createEvent(eventForm);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Event '" + eventForm.getSummary() + "' created successfully!"
            );

            return "redirect:/myevents-today";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Failed to create event: " + e.getMessage()
            );
            return "redirect:/events/add";
        }
    }
}