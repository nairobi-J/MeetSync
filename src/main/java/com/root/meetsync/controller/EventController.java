package com.root.meetsync.controller;

import com.root.meetsync.dto.CreateEventRequest;
import com.root.meetsync.entity.Event;
import com.root.meetsync.service.EventService;
import org.springframework.security.core.Authentication; // Updated import
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/create")
    public String createEvent(CreateEventRequest request, Authentication auth) {

       Event savedEvent = eventService.createEvent(request, auth);
        return "redirect:/event/" + savedEvent.getShareLink();
    }
}