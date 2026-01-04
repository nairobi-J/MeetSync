package com.root.meetsync.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.root.meetsync.dto.UserResponseDto;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.repository.EventSlotRepository;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.service.AvailabilityService;

@Controller
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private EventSlotRepository eventSlotRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/availability/submit")
    public ResponseEntity<String> submitAvailability(@RequestParam UUID userId, @RequestBody List<UUID> slotIds) {

        UserResponseDto user = userRepository.findById(userId)
                .map(u -> new UserResponseDto(u.getId()))
                .orElse(null);

        List<EventSlot> slots = eventSlotRepository.findAllById(slotIds);

        availabilityService.saveAvailability(user, slots);
        return ResponseEntity.ok("Availability submitted successfully");
    }

    
}
