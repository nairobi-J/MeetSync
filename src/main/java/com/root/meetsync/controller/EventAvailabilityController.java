package com.root.meetsync.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.root.meetsync.entity.Event;
import com.root.meetsync.dto.SlotCountDto;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.User;
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

    @PostMapping("/availability/submit")
    public String submitAvailability(@RequestParam String participantName, @RequestParam List<Long> slotIds) {

        availabilityService.saveAvailability(participantName, slotIds);

       return "redirect:/guest-view";
    } 

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
    
    List<LocalTime> hours = IntStream.range(0, 24)
            .mapToObj(h -> LocalTime.of(h, 0))
            .collect(Collectors.toList());

    model.addAttribute("event", event);
    model.addAttribute("hours", hours);
    // This should return a Map<String, List<String>> or similar for the heatmap
    model.addAttribute("heatmapData", availabilityService.getHeatmapStat(eventId)); 
    
    return "guest-view"; // Make sure your HTML file is named guest-view.html
}

}