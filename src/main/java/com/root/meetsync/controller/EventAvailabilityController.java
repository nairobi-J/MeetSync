package com.root.meetsync.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.repository.EventRepository;
import com.root.meetsync.repository.EventSlotRepository;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.service.AvailabilityService;

@Controller
public class EventAvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventSlotRepository eventSlotRepository;

    @Autowired
    EventRepository eventRepository;

public List<LocalTime> generateTimeSlots(LocalTime earliest, LocalTime latest) {
    List<LocalTime> slots = new ArrayList<>();
    LocalTime current = earliest;
    
    System.out.println("Generating time slots from " + earliest + " to " + latest);
    
    while (current.isBefore(latest)) {
        slots.add(current);
        System.out.println("Added time slot: " + current);
        current = current.plusHours(1);
    }
    
    System.out.println("Generated " + slots.size() + " time slots total");
    return slots;
}

@PostMapping("/event/participant/submit")
public String submitAvailability(
        @RequestParam String participantName, 
        @RequestParam String slotIds,
        @RequestParam Long eventId,
         RedirectAttributes redirectAttributes) {

   
    List<Long> slotIdList = java.util.Arrays.stream(slotIds.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::parseLong)
            .collect(Collectors.toList());

    try {
        availabilityService.saveAvailability(participantName, slotIdList, eventId);
    } catch (IllegalArgumentException e) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/";
    }

    return "redirect:/";
}


private void populateGuestModel(Model model, Event event) {
    // time slots based on event's earliest and latest times
    List<LocalTime> hours = generateTimeSlots(event.getEarliestTime(), event.getLatestTime());
    
    // unique dates from event slots
    List<LocalDate> dates = event.getSlots().stream()
            .map(slot -> slot.getSlotDate())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    
    //  map "date_time" -> slot
    Map<String, EventSlot> slotMap = event.getSlots().stream()
            .collect(Collectors.toMap(
                slot -> slot.getSlotDate().toString() + "_" + slot.getStartTime().toString(),
                slot -> slot
            ));
  
    model.addAttribute("event", event);
    model.addAttribute("hours", hours);
    model.addAttribute("dates", dates);
    model.addAttribute("slotMap", slotMap);
    model.addAttribute("heatmapData", availabilityService.getHeatmapDataForGuestView(event.getId()));

}

 @GetMapping("/event/participant/{shareLink}")
 public String showEventAvailabilityPage(@PathVariable String shareLink, Model model, Authentication auth) {
        Event event = eventRepository.findByShareLink(shareLink)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        
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
    
        populateGuestModel(model, event);

        if (isHost) {
            
            String fullUrl = "http://localhost:8080/event/participant/" + shareLink;
            model.addAttribute("fullUrl", fullUrl);
            return "host-view";
        } else {
            return "guest-view"; 
        }
    }


}