package com.root.meetsync.controller;


import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.root.meetsync.service.impl.GoogleCalendarService;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EventController {

    private final GoogleCalendarService calendarService;

    public EventController(GoogleCalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @GetMapping("/events")
    public Events getEvents() throws Exception {
        return calendarService.getEvents();
    }

    @GetMapping("/events/today")
    public Events getTodayEvents() throws Exception {
        return calendarService.getTodayEvents();
    }




//    Add Event




}
