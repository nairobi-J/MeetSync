package com.root.meetsync.controller.event;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.dto.event.EventListDTO;
import com.root.meetsync.service.event.EventTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller

@RequiredArgsConstructor
@RequestMapping("/events")
public class EventTestController {

    private final EventTestService eventTestService;


    /* =========================
       List all events
       ========================= */
    @GetMapping
    public String eventsPage(
            @ModelAttribute("currentUser") CurrentUserDTO currentUser,
            Model model
    ) {
        List<EventListDTO> events = eventTestService.getEventsForUser(currentUser);
        model.addAttribute("events",events
                );
        model.addAttribute("hasEvents", !events.isEmpty());

        return "events/EventsPage";
//        return "fragments/events/events-list";
    }

    /* =========================
       Event details
       ========================= */
    @GetMapping("/{id}")
    public String eventDetails(
            @PathVariable Long id,
            @ModelAttribute("currentUser") CurrentUserDTO currentUser,
            Model model
    ) {
        model.addAttribute("event",
                eventTestService.getEventDetails(id, currentUser));

        return "fragments/events/event-details";
    }




}
