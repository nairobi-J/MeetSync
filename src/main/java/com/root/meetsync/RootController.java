package com.root.meetsync;

import com.root.meetsync.entity.Event;
import com.root.meetsync.repository.EventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
    public String showCreateEventPage() {
        return "create-event";
    }

    @GetMapping("/event/{shareLink}")
    public String showEventPage(@PathVariable String shareLink, Model model, Authentication auth) {
        Event event = eventRepository.findByShareLink(shareLink)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // logic to generate time slots
        List<LocalTime> hours = new ArrayList<>();
        LocalTime current = event.getEarliestTime();
        while (current.isBefore(event.getLatestTime())) {
            hours.add(current);
            current = current.plusHours(1);
        }

        model.addAttribute("event", event);
        model.addAttribute("hours", hours);

        // --- HOST CHECK LOGIC ---
        boolean isHost = false;
        if (auth != null && auth.isAuthenticated()) {
            String currentUserEmail;
            if (auth instanceof OAuth2AuthenticationToken oauth) {
                currentUserEmail = (String) oauth.getPrincipal().getAttributes().get("email");
            } else {
                currentUserEmail = auth.getName();
            }

            if (event.getHost().getEmail().equals(currentUserEmail)) {
                isHost = true;
            }
        }

        if (isHost) {
            // Add the full URL for the copy-link feature
            String fullUrl = "http://localhost:8080/event/" + shareLink;
            model.addAttribute("fullUrl", fullUrl);
            return "host-view"; // Return host-view.html
        } else {
            return "guest-view"; // Return guest-view.html
        }
    }
}