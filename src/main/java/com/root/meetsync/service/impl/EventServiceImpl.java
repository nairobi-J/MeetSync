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
    // Inject only what you need
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Event createEvent(CreateEventRequest request, OAuth2AuthenticationToken auth) {
        User host = userRepository.findByGoogleId((String) auth.getPrincipal().getAttributes().get("sub"))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = new Event();
        event.setHost(host);
        event.setTitle(request.getTitle());
        event.setTimezone(request.getTimezone());
        event.setEarliestTime(request.getEarliestTime());
        event.setLatestTime(request.getLatestTime());
        event.setSlotDuration(60);
        event.setShareLink(UUID.randomUUID().toString());

        // Map the List<LocalDate> directly to EventSlots
        List<EventSlot> slots = request.getSelectedDates().stream().map(date -> {
            EventSlot slot = new EventSlot();
            slot.setEvent(event);
            slot.setSlotDate(date);
            slot.setStartTime(request.getEarliestTime());
            slot.setEndTime(request.getEarliestTime().plusHours(1));

            // Link HostAvailability here if your Entity has a List<HostAvailability>
            // This allows JPA to save everything in one go.
            return slot;
        }).collect(Collectors.toList());

        event.setSlots(slots);
        return eventRepository.save(event); // One save call handles it all
    }
}