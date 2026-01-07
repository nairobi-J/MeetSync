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

    // @PostMapping("/availability/submit")
    // public String submitAvailability(@RequestParam String participantName, @RequestParam List<Long> slotIds) {

    //     availabilityService.saveAvailability(participantName, slotIds);

    //    return "redirect:/guest-view";
    // } 

//    @GetMapping("/availability/test/{userId}/{eventId}")
//    public String showActualForm(@PathVariable UUID userId, @PathVariable UUID eventId, Model model) {

//       User user = userRepository.findById(userId).get();
//       List<EventSlot> slots = eventSlotRepository.findByEventId(eventId);
    
    
//       model.addAttribute("user", user);
//       model.addAttribute("slots", slots);
    
//       return "demo_submit";
//     }

//     @GetMapping("/event/{eventId}/heatmap")
//      public String showHeatmap(@PathVariable UUID eventId, Model model) {
//        Map<LocalDate, List<SlotCountDto>> stats = availabilityService.getHeatmapStat(eventId);

//         Event event = eventRepository.findById(eventId).orElse(null);

//         model.addAttribute("stats", stats);
//         model.addAttribute("event", event);
    
//     return "demo_success"; 
//     }

  @GetMapping("/event/{eventId}/guest-view")
public String showGuestView(@PathVariable Long eventId, Model model) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new RuntimeException("Event not found"));
    
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
  
    System.out.println("=== DEBUG: Slot Map Keys ===");
    System.out.println("Total slots in event: " + event.getSlots().size());
    event.getSlots().forEach(slot -> 
        System.out.println("Slot ID: " + slot.getId() + 
                          ", Date: " + slot.getSlotDate() + 
                          ", Time: " + slot.getStartTime() + 
                          ", Key would be: " + slot.getSlotDate().toString() + "_" + slot.getStartTime().toString())
    );
    
    slotMap.keySet().forEach(key -> System.out.println("Slot Map Key: " + key + " -> Slot ID: " + slotMap.get(key).getId()));
    System.out.println("Event earliest time: " + event.getEarliestTime());
    System.out.println("Event latest time: " + event.getLatestTime());
    System.out.println("Generated hours: " + hours);
    System.out.println("Unique dates: " + dates);
    System.out.println("=== END DEBUG ===");

    model.addAttribute("event", event);
    model.addAttribute("hours", hours);
    model.addAttribute("dates", dates);
    model.addAttribute("slotMap", slotMap);
    model.addAttribute("heatmapData", availabilityService.getHeatmapDataForGuestView(eventId)); 

    return "guest-view";
}

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
        @RequestParam Long eventId) {

   
    List<Long> slotIdList = java.util.Arrays.stream(slotIds.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(Long::parseLong)
            .collect(Collectors.toList());

    availabilityService.saveAvailability(participantName, slotIdList);

    return "redirect:/event/" + eventId + "/guest-view";
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
            // Add the full URL for the copy-link feature
            String fullUrl = "http://localhost:8080/event/participant/" + shareLink;
            model.addAttribute("fullUrl", fullUrl);
            return "host-view"; // Return host-view.html
        } else {
            return "guest-view"; // Return guest-view.html
        }
    }


}