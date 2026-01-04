package com.root.meetsync.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.root.meetsync.dto.SlotCountDto;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.User;
import com.root.meetsync.repository.EventSlotRepository;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.service.AvailabilityService;

@Controller
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EventSlotRepository eventSlotRepository;

    @PostMapping("/availability/submit")
    public String submitAvailability(@RequestParam UUID userId, @RequestParam List<UUID> slotIds) {

        availabilityService.saveAvailability(userId, slotIds);

       return "redirect:/availability/success";
    } 

   @GetMapping("/availability/test/{userId}/{eventId}")
   public String showActualForm(@PathVariable UUID userId, @PathVariable UUID eventId, Model model) {

      User user = userRepository.findById(userId).get();
      List<EventSlot> slots = eventSlotRepository.findByEventId(eventId);
    
    
      model.addAttribute("user", user);
      model.addAttribute("slots", slots);
    
      return "demo_submit";
    }

    @GetMapping("/availability/success")
      public String showSuccess() {
       return "demo_success";
     }

    @GetMapping("/event/{eventId}/heatmap")
     public String showHeatmap(@PathVariable UUID eventId, Model model) {
        List<SlotCountDto> stats = availabilityService.getHeatmapStat(eventId);
    
        model.addAttribute("stats", stats);
        model.addAttribute("eventId", eventId);
    
    return "demo_success"; 
    }
}
