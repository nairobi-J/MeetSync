package com.root.meetsync;


import com.root.meetsync.entity.*;
import com.root.meetsync.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalTime;
import java.util.*;

@Controller
public class RootController {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventSlotRepository eventSlotRepository;
    private final HostAvailabilityRepository hostAvailabilityRepository;
    private final ParticipantAvailabilityRepository participantAvailabilityRepository;
    private final ConfirmedEventRepository confirmedEventRepository;
    public RootController(EventRepository eventRepository, EventSlotRepository eventSlotRepository,
                          HostAvailabilityRepository hostAvailabilityRepository, UserRepository userRepository,
                          ParticipantAvailabilityRepository participantAvailabilityRepository, ConfirmedEventRepository confirmedEventRepository) {
        this.eventRepository = eventRepository;
        this.eventSlotRepository = eventSlotRepository;
        this.hostAvailabilityRepository = hostAvailabilityRepository;
        this.userRepository = userRepository;
        this.participantAvailabilityRepository = participantAvailabilityRepository;
        this.confirmedEventRepository = confirmedEventRepository;
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

    @Transactional
    @PostMapping("/event/{shareLink}/host-availability")
    public String saveHostAvailability(@PathVariable String shareLink, @RequestParam(required = false) List<Long> slotIds, Authentication auth) {
Event event = eventRepository.findByShareLink(shareLink)
              .orElseThrow(() -> new RuntimeException("Event not found"));
        String email = getEmailFromAuth(auth);
        User host = userRepository.findByEmail(email).orElseThrow();

        hostAvailabilityRepository.deleteByHostAndEventSlot_Event_Id(host, event.getId());

        if (slotIds != null && !slotIds.isEmpty()) {
            List<HostAvailability> newAvailabilities = new ArrayList<>();

            for (Long id : slotIds) {
                HostAvailability ha = new HostAvailability();
                ha.setHost(host);

                // USE THIS: getReferenceById doesn't hit the database!
                // It just creates a "proxy" using the ID you already have.
                ha.setEventSlot(eventSlotRepository.getReferenceById(id));

                newAvailabilities.add(ha);
            }
            // 3. Save all at once
            hostAvailabilityRepository.saveAll(newAvailabilities);
        }
        return "redirect:/event/" + shareLink;
    }


@GetMapping("/event/{shareLink}/confirm")
public String showConfirmEventPage(@PathVariable String shareLink, Model model){
        Event event = eventRepository.findByShareLink(shareLink).orElseThrow(() -> new RuntimeException("event not found"));
        model.addAttribute("event", event);
        return "confirm-event";
}


@Transactional
@PostMapping("event/{shareLink}/confirm")
public String finalizeEvent(@PathVariable String shareLink, @RequestParam Long slotId){
    Event event = eventRepository.findByShareLink(shareLink).orElseThrow(()-> new RuntimeException("vai event e nai"));
    EventSlot selectedSlot = eventSlotRepository.findById(slotId).orElseThrow();

    ConfirmedEvent confirmed = new ConfirmedEvent();
    confirmed.setEvent(event);
    confirmed.setSelectedSlots(selectedSlot);
    confirmed.setConfirmedAt(java.time.LocalDateTime.now());

    confirmedEventRepository.save(confirmed);
    return "redirect:/event/" + shareLink + "/overview";
}




    @GetMapping("/event/{shareLink}/overview")
    public String showEventOverview(@PathVariable String shareLink, Model model, Authentication auth) {
        Event event = eventRepository.findByShareLink(shareLink)
                .orElseThrow();
        Optional<ConfirmedEvent> confirmedEvent = confirmedEventRepository.findByEvent_Id(event.getId());
        // 1. Setup Time Headers for the grid
        List<LocalTime> hours = new ArrayList<>();
        LocalTime current = event.getEarliestTime();
        while (current.isBefore(event.getLatestTime())) {
            hours.add(current);
            current = current.plusHours(1);
        }

        // 2. Prepare Combined Heatmap Data (Host + Participants);
        Map<Long, List<String>> combinedHeatmap = new HashMap<>();
        // Initialize every slot with an empty list
        event.getSlots().forEach(slot -> combinedHeatmap.put(slot.getId(), new ArrayList<>()));

        // Add Host Selections
        List<HostAvailability> hostVotes = hostAvailabilityRepository.findByEventSlot_Event_Id(event.getId());
        for (HostAvailability ha : hostVotes) {
            String hostName = ha.getHost().getName() != null ? ha.getHost().getName() : "Host";
            combinedHeatmap.get(ha.getEventSlot().getId()).add(hostName + " (Host)");
        }

        // Add Participant Selections
        List<ParticipantAvailability> guestVotes = participantAvailabilityRepository.findByEventSlot_Event_Id(event.getId());
        for (ParticipantAvailability pa : guestVotes) {
            combinedHeatmap.get(pa.getEventSlot().getId()).add(pa.getParticipantName());
        }

        // 3. Calculate Statistics (Best Time & Total People)
        long maxVotes = 0;
        EventSlot bestSlot = null;
        for (EventSlot slot : event.getSlots()) {
            int count = combinedHeatmap.get(slot.getId()).size();
            if (count > maxVotes) {
                maxVotes = count;
                bestSlot = slot;
            }
        }

        // Unique count of guest names + 1 for the Host
        long uniqueGuests = guestVotes.stream().map(ParticipantAvailability::getParticipantName).distinct().count();

        model.addAttribute("event", event);
        model.addAttribute("hours", hours);
        model.addAttribute("heatmapDataJson", convertMapToJson(combinedHeatmap)); // Uses your helper method
        model.addAttribute("bestSlot", bestSlot);
        model.addAttribute("totalParticipants", uniqueGuests + 1);
        model.addAttribute("confirmedEvent", confirmedEvent.orElse(null));
        return "event-overview";
    }
    @Transactional
    @PostMapping("/event/{shareLink}/reschedule")
    public String rescheduledEvent(@PathVariable String shareLink){
        Event event = eventRepository.findByShareLink(shareLink).orElseThrow();
        confirmedEventRepository.deleteByEvent_Id(event.getId());
        return "redirect:/event/" + shareLink + "/overview";

    }


    private String convertMapToJson(Map<Long, List<String>> map) {
        StringBuilder sb = new StringBuilder("{");
        Iterator<Map.Entry<Long, List<String>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, List<String>> entry = it.next();
            sb.append("\"").append(entry.getKey()).append("\":[");
            List<String> names = entry.getValue();
            for (int j = 0; j < names.size(); j++) {
                sb.append("\"").append(names.get(j)).append("\"");
                if (j < names.size() - 1) sb.append(",");
            }
            sb.append("]");
            if (it.hasNext()) sb.append(",");
        }
        return sb.append("}").toString();
    }


    private String getEmailFromAuth(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken oauth) return (String) oauth.getPrincipal().getAttributes().get("email");
        return auth != null ? auth.getName() : null;
    }
}
