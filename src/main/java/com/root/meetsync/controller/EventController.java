package com.root.meetsync.controller;

import com.root.meetsync.dto.CreateEventRequest;
import com.root.meetsync.entity.Event;
import com.root.meetsync.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/create")
    public String createEvent(@Valid @ModelAttribute CreateEventRequest request, 
                            BindingResult bindingResult,
                            Authentication auth,
                            RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("Please fix the following errors: ");
            bindingResult.getFieldErrors().forEach(error -> 
                errorMessage.append(error.getDefaultMessage()).append("; ")
            );
            redirectAttributes.addFlashAttribute("errorMessage", errorMessage.toString());
           
        
            return "redirect:/create-event";
        }

        try {
            // Check for duplicate event name
            if (eventService.eventExistsByTitleAndUser(request.getTitle(), auth)) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "An event with this name already exists. Please choose a different name.");
                return "redirect:/create-event";
            }

            Event savedEvent = eventService.createEvent(request, auth);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Event '" + savedEvent.getTitle() + "' created successfully!");
// 








            return "redirect:/event/" + savedEvent.getShareLink();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Failed to create event: " + e.getMessage());
            return "redirect:/create-event";
        }
    }

    @GetMapping("/check-name")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEventName(@RequestParam String title, Authentication auth) {
        Map<String, Boolean> response = new HashMap<>();
        try {
            boolean exists = eventService.eventExistsByTitleAndUser(title, auth);
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("exists", false);
            return ResponseEntity.ok(response);
        }
    }
}