package com.root.meetsync;

import com.root.meetsync.entity.Event;
import com.root.meetsync.repository.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RootController {
    private final EventRepository eventRepository;

    public RootController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping("/event-create")
    public String showCreateEventPage(){
        return "create-event";
    }

    @GetMapping("event/{shareLink}")
    public String showGuestView(@PathVariable String shareLink, Model model){
      Event event = eventRepository.findByShareLink(shareLink)
              .orElseThrow(() -> new RuntimeException("Event not found"));
        List<LocalTime>hours = new ArrayList<>();
        LocalTime current = event.getEarliestTime();
        while(current.isBefore(event.getLatestTime())){
            hours.add(current);
            current = current.plusHours(1);
        }
        model.addAttribute("event", event);
        model.addAttribute("hours", hours);
        return "guest-view";
    }

}
