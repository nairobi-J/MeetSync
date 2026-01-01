package com.root.meetsync.service.impl;


import com.root.meetsync.dto.CreateEventRequest;
import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.HostAvailability;
import com.root.meetsync.entity.User;
import com.root.meetsync.repository.EventRepository;
import com.root.meetsync.repository.HostAvailabilityRepository;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.service.EventService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final HostAvailabilityRepository hostAvailabilityRepository; // Add this

    public EventServiceImpl(EventRepository eventRepository,
                            UserRepository userRepository,
                            HostAvailabilityRepository hostAvailabilityRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.hostAvailabilityRepository = hostAvailabilityRepository;
    }

    @Override
    @Transactional
    public Event createEvent(CreateEventRequest request, OAuth2AuthenticationToken auth) {
        String googleId = (String) auth.getPrincipal().getAttributes().get("sub");
        User host = userRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Create the Event Policy
        Event event = new Event();
        event.setHost(host);
        event.setTitle(request.getTitle());
        event.setTimezone(request.getTimezone());
        event.setEarliestTime(request.getEarliestTime());
        event.setLatestTime(request.getLatestTime());
        event.setSlotDuration(60);
        event.setShareLink(UUID.randomUUID().toString());

        // 2. Validate and Map Slots
        List<EventSlot> slots = request.getSelectedSlots().stream().map(s -> {
            LocalTime slotStart = s.getStart();
            LocalTime slotEnd = slotStart.plusHours(1);

            // VALIDATION RULE:
            if (slotStart.isBefore(request.getEarliestTime()) || slotEnd.isAfter(request.getLatestTime())) {
                throw new RuntimeException("Error: Slot " + slotStart + " on " + s.getDate() +
                        " is outside the allowed range of " +
                        request.getEarliestTime() + " to " + request.getLatestTime());
            }

            EventSlot slot = new EventSlot();
            slot.setEvent(event);
            slot.setSlotDate(s.getDate());
            slot.setStartTime(slotStart);
            slot.setEndTime(slotEnd);
            return slot;
        }).collect(Collectors.toList());

        event.setSlots(slots);
        Event savedEvent = eventRepository.save(event);

        // 3. Mark Host Availability
        for (EventSlot slot : savedEvent.getSlots()) {
            HostAvailability hostAvail = new HostAvailability();
            hostAvail.setHost(host);
            hostAvail.setEventSlot(slot);
            hostAvailabilityRepository.save(hostAvail);
        }

        return savedEvent;
    }


}