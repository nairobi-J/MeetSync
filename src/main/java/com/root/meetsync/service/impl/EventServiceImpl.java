package com.root.meetsync.service.impl;

import com.root.meetsync.dto.CreateEventRequest;
import com.root.meetsync.entity.*;
import com.root.meetsync.repository.*;
import com.root.meetsync.service.EventService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Event createEvent(CreateEventRequest request, Authentication auth) {
        String email;

        // Extract email based on the type of login
        if (auth instanceof OAuth2AuthenticationToken oauth) {
            email = (String) oauth.getPrincipal().getAttributes().get("email");
        } else {
            email = auth.getName();
        }

        User host = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        Event event = new Event();
        event.setHost(host);
        event.setTitle(request.getTitle());
        event.setTimezone(request.getTimezone());
        event.setEarliestTime(request.getEarliestTime());
        event.setLatestTime(request.getLatestTime());

        // Use default to 60min duration if not provided
        Integer slotDuration = request.getSlotDuration() != null ? request.getSlotDuration() : 60;
        event.setSlotDuration(slotDuration);
        event.setShareLink(UUID.randomUUID().toString());

        if (request.getSelectedDates() != null) {
            List<EventSlot> slots = new ArrayList<>();

            for (LocalDate date : request.getSelectedDates()) {
                LocalTime timeTracker = request.getEarliestTime();

                while (timeTracker.isBefore(request.getLatestTime())) {
                    EventSlot slot = new EventSlot();
                    slot.setEvent(event);
                    slot.setSlotDate(date);
                    slot.setStartTime(timeTracker);
                    
                    LocalTime endTime = timeTracker.plusMinutes(slotDuration);
                    slot.setEndTime(endTime);
                    slots.add(slot);

                    // Move to next slot
                    timeTracker = timeTracker.plusMinutes(slotDuration);
                }
            }
            event.setSlots(slots);
        }

        return eventRepository.save(event);
    }

    @Override
    public boolean eventExistsByTitleAndUser(String title, Authentication auth) {
        String email;

        // Extract email based on the type of login
        if (auth instanceof OAuth2AuthenticationToken oauth) {
            email = (String) oauth.getPrincipal().getAttributes().get("email");
        } else {
            email = auth.getName();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return eventRepository.existsByTitleAndHost(title, user);
    }
}