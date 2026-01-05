package com.root.meetsync.controller;

import com.root.meetsync.dto.CreateEventRequest;
import com.root.meetsync.entity.Event;
import com.root.meetsync.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/create")
    public String createEvent(CreateEventRequest request,
                                             OAuth2AuthenticationToken auth) {
        eventService.createEvent(request, auth);
        return "redirect:/";
    }
}