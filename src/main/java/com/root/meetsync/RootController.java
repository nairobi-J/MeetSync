package com.root.meetsync;

import com.root.meetsync.entity.*;
import com.root.meetsync.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.*;

@Controller
public class RootController {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventSlotRepository eventSlotRepository;
    private final HostAvailabilityRepository hostAvailabilityRepository;

    public RootController(EventRepository eventRepository, EventSlotRepository eventSlotRepository, HostAvailabilityRepository hostAvailabilityRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.eventSlotRepository = eventSlotRepository;
        this.hostAvailabilityRepository = hostAvailabilityRepository;
        this.userRepository = userRepository;
    }
    @GetMapping("/event-create")
    public String showCreateEventPage() {
        return "create-event";
    }

    @GetMapping("/event/{shareLink}")
    public String showEventPage(@PathVariable String shareLink, Model model, Authentication auth) {
        Event event = eventRepository.findByShareLink(shareLink)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        List<LocalTime> hours = new ArrayList<>();
        LocalTime current = event.getEarliestTime();
        while (current.isBefore(event.getLatestTime())) {
            hours.add(current);
            current = current.plusHours(1);
        }

        String email = getEmailFromAuth(auth);
        List<Long> savedHostSlotIds = new ArrayList<>();

        // Manual JSON construction to avoid "brainless" databind errors
        StringBuilder jsonBuilder = new StringBuilder("{");

        List<HostAvailability> allAvails = hostAvailabilityRepository.findByEventSlot_Event_Id(event.getId());
        Map<Long, List<String>> map = new HashMap<>();

        for (HostAvailability ha : allAvails) {
            Long sId = ha.getEventSlot().getId();
            map.computeIfAbsent(sId, k -> new ArrayList<>()).add(ha.getHost().getName());
            if (email != null && ha.getHost().getEmail().equals(email)) {
                savedHostSlotIds.add(sId);
            }
        }

        // Convert map to simple JSON string manually
        int i = 0;
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            jsonBuilder.append("\"").append(entry.getKey()).append("\":[");
            for (int j = 0; j < entry.getValue().size(); j++) {
                jsonBuilder.append("\"").append(entry.getValue().get(j)).append("\"");
                if (j < entry.getValue().size() - 1) jsonBuilder.append(",");
            }
            jsonBuilder.append("]");
            if (i < map.size() - 1) jsonBuilder.append(",");
            i++;
        }
        jsonBuilder.append("}");

        model.addAttribute("event", event);
        model.addAttribute("hours", hours);
        model.addAttribute("savedHostSlotIds", savedHostSlotIds);
        model.addAttribute("heatmapDataJson", jsonBuilder.toString());
        model.addAttribute("fullUrl", "http://localhost:8080/event/" + shareLink);

        if (email != null && event.getHost().getEmail().equals(email)) {
            return "host-view";
        }
        return "guest-view";
    }

    @PostMapping("/event/{shareLink}/host-availability")
    public String saveHostAvailability(@PathVariable String shareLink, @RequestParam(required = false) List<Long> slotIds, Authentication auth) {
        Event event = eventRepository.findByShareLink(shareLink).orElseThrow();
        String email = getEmailFromAuth(auth);
        User host = userRepository.findByEmail(email).orElseThrow();

        hostAvailabilityRepository.deleteByHostAndEventSlot_Event_Id(host, event.getId());

        if (slotIds != null) {
            for (Long id : slotIds) {
                eventSlotRepository.findById(id).ifPresent(slot -> {
                    HostAvailability ha = new HostAvailability();
                    ha.setHost(host);
                    ha.setEventSlot(slot);
                    hostAvailabilityRepository.save(ha);
                });
            }
        }
        return "redirect:/event/" + shareLink;
    }


    private String getEmailFromAuth(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken oauth) return (String) oauth.getPrincipal().getAttributes().get("email");
        return auth != null ? auth.getName() : null;
    }
}