package com.root.meetsync.service.event;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.dto.event.EventDetailsDTO;
import com.root.meetsync.dto.event.EventListDTO;
import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.User;
import com.root.meetsync.repository.event.EventTestRepository;
import com.root.meetsync.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventTestService {

    private final EventTestRepository eventTestRepository;
    private final UserService userService;


    public EventTestService(EventTestRepository eventTestRepository, UserService userService) {
        this.eventTestRepository = eventTestRepository;
        this.userService = userService;
    }

    //all events
    public List<EventListDTO> getEventsForUser(CurrentUserDTO currentUser) {

        User user = userService.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return eventTestRepository.findAllByHost(user)
                .stream()
                .map(event -> EventListDTO.builder()
                        .id(event.getId())
                        .title(event.getTitle())
                        .timezone(event.getTimezone())
                        .shareLink(event.getShareLink())
                        .build())
                .toList();
    }

    /* =========================
       Single event by id
       ========================= */
    public EventDetailsDTO getEventDetails(Long eventId, CurrentUserDTO currentUser) {

        User user = userService.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventTestRepository.findByIdAndHost(eventId, user)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return EventDetailsDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .timezone(event.getTimezone())
                .earliestTime(event.getEarliestTime())
                .latestTime(event.getLatestTime())
                .slotDuration(event.getSlotDuration())
                .shareLink(event.getShareLink())
                .createdAt(event.getCreatedAt())
                .build();
    }


}
