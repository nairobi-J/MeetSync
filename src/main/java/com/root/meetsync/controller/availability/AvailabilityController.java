package com.root.meetsync.controller.availability;

import com.root.meetsync.dto.availability.AvailableSlotDTO;
import com.root.meetsync.dto.availability.SetupAvailabilityRequest;
import com.root.meetsync.service.availability.IAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final IAvailabilityService availabilityService;

    @PostMapping("/setup")
    public ResponseEntity<String> setupAvailability(
            @RequestHeader("User-Id") UUID userId,
            @RequestBody SetupAvailabilityRequest request) {
        availabilityService.setupAvailability(userId, request);
        return ResponseEntity.ok("Availability setup successfully");
    }

    @GetMapping("/link")
    public ResponseEntity<String> getBookingLink(@RequestHeader("User-Id") UUID userId) {
        String link = availabilityService.getUserBookingLink(userId);
        return ResponseEntity.ok(link);
    }

    @GetMapping("/u/{emailPrefix}")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @PathVariable String emailPrefix,
            @RequestParam(required = false) String timezone) {
        List<AvailableSlotDTO> slots = availabilityService.getAvailableSlots(emailPrefix, timezone);
        return ResponseEntity.ok(slots);
    }

  
}
