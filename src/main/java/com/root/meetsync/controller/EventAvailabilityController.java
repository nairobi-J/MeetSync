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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.repository.EventRepository;
import com.root.meetsync.repository.EventSlotRepository;
import com.root.meetsync.repository.ParticipantAvailabilityRepository;
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

    @Autowired
    ParticipantAvailabilityRepository participantAvailabilityRepository;

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
        // Check if this is an update
        boolean isUpdate = participantAvailabilityRepository.existsByParticipantNameAndEventSlot_Event_Id(participantName, eventId);
        
        availabilityService.saveAvailability(participantName, slotIdList, eventId);
        
    
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
       
        String successMessage = isUpdate ? 
            "Your availability has been updated successfully!" : 
            "Your availability has been saved successfully!";
        redirectAttributes.addFlashAttribute("successMessage", successMessage);
        redirectAttributes.addFlashAttribute("participantName", participantName);
        redirectAttributes.addFlashAttribute("submittedSlots", slotIds);
        
        return "redirect:/event/participant/" + event.getShareLink() + "?submitted=true";
    } catch (Exception e) {
        
        Event event = eventRepository.findById(eventId)
                .orElse(null);
        
        redirectAttributes.addFlashAttribute("errorMessage", "Failed to save availability: " + e.getMessage());
        
        if (event != null) {
            return "redirect:/event/participant/" + event.getShareLink();
        } else {
            return "redirect:/";
        }
    }
}


// private void populateGuestModel(Model model, Event event) {
//     populateGuestModel(model, event, false);
// }

private void populateGuestModel(Model model, Event event, boolean isHost) {
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
    model.addAttribute("isHost", isHost);
    
    if (isHost) {
        
        model.addAttribute("heatmapData", availabilityService.getHeatmapDataForHostView(event.getId()));
    } else {
       
        model.addAttribute("heatmapData", availabilityService.getHeatmapDataForParticipant(event.getId()));
    }
}

 @GetMapping("/event/participant/{shareLink}")
 public String showEventAvailabilityPage(@PathVariable String shareLink, Model model, Authentication auth) {
        Event event = eventRepository.findByShareLink(shareLink)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        boolean isHost = false;
        
     
        if (auth != null && auth.isAuthenticated()) {
            try {
                String currentUserEmail = null;
                if (auth instanceof OAuth2AuthenticationToken oauth) {
                    currentUserEmail = (String) oauth.getPrincipal().getAttributes().get("email");
                } else {
                    currentUserEmail = auth.getName();
                }
                
                if (currentUserEmail != null && event.getHost().getEmail().equals(currentUserEmail)) {
                    isHost = true;
                }
            } catch (Exception e) {
    
                System.err.println("Error checking host status: " + e.getMessage());
            }
        }
    
        populateGuestModel(model, event, isHost);

        if (isHost) {
            
            return "redirect:/event/" + shareLink;
        } else {
            return "guest-view"; 
        }
    }

    @GetMapping("/event/participant/{shareLink}/edit/{participantName}")
    public String showEditAvailabilityPage(@PathVariable String shareLink, 
                                          @PathVariable String participantName, 
                                          Model model, 
                                          Authentication auth) {
        Event event = eventRepository.findByShareLink(shareLink)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Check if participant has previously submitted
        if (!availabilityService.hasUserSubmitted(participantName, event.getId())) {
            // If user hasn't submitted before, redirect to normal submission page
            return "redirect:/event/participant/" + shareLink;
        }

        // Check if user is the host (redirect to host view)
        boolean isHost = false;
        if (auth != null && auth.isAuthenticated()) {
            try {
                String currentUserEmail = null;
                if (auth instanceof OAuth2AuthenticationToken oauth) {
                    currentUserEmail = (String) oauth.getPrincipal().getAttributes().get("email");
                } else {
                    currentUserEmail = auth.getName();
                }
                
                if (currentUserEmail != null && event.getHost().getEmail().equals(currentUserEmail)) {
                    isHost = true;
                }
            } catch (Exception e) {
                System.err.println("Error checking host status: " + e.getMessage());
            }
        }

        if (isHost) {
            return "redirect:/event/" + shareLink;
        }

        // Get user's existing selections
        List<Long> existingSelections = availabilityService.getUserAvailability(participantName, event.getId());
        
        // Populate the model with event data and edit-specific data
        populateGuestModel(model, event, false); 
        model.addAttribute("editMode", true);
        model.addAttribute("participantName", participantName);
        model.addAttribute("existingSelections", existingSelections);
        
        return "guest-view";
    }

    @GetMapping("/event/participant/{shareLink}/availability/{participantName}")
    @ResponseBody
    public List<Long> getUserAvailability(@PathVariable String shareLink, @PathVariable String participantName) {
        Event event = eventRepository.findByShareLink(shareLink)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        
        return availabilityService.getUserAvailability(participantName, event.getId());
    }
}