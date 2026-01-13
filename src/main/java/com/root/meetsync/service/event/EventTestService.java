package com.root.meetsync.service.event;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.dto.event.EventDetailsDTO;
import com.root.meetsync.dto.event.EventListDTO;
import com.root.meetsync.entity.ConfirmedEvent;
import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.User;
import com.root.meetsync.repository.ConfirmedEventRepository;
import com.root.meetsync.repository.event.EventTestRepository;
import com.root.meetsync.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventTestService {

    private final EventTestRepository eventTestRepository;
    private final UserService userService;
    private final ConfirmedEventRepository confirmedEventRepository;


    public EventTestService(EventTestRepository eventTestRepository, 
                           UserService userService,
                           ConfirmedEventRepository confirmedEventRepository) {
        this.eventTestRepository = eventTestRepository;
        this.userService = userService;
        this.confirmedEventRepository = confirmedEventRepository;
    }

    //all events
    public List<EventListDTO> getEventsForUser(CurrentUserDTO currentUser) {

        User user = userService.findByEmail(currentUser.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return eventTestRepository.findAllByHost(user)
                .stream()
                .map(event -> {
                    EventListDTO.EventListDTOBuilder builder = EventListDTO.builder()
                            .id(event.getId())
                            .title(event.getTitle())
                            .timezone(event.getTimezone())
                            .shareLink(event.getShareLink())
                            .earliestTime(event.getEarliestTime())
                            .latestTime(event.getLatestTime())
                            .slotDuration(event.getSlotDuration());
                    
                    // Check if event has a confirmed slot
                    Optional<ConfirmedEvent> confirmedEvent = confirmedEventRepository.findByEvent_Id(event.getId());
                    
                    if (confirmedEvent.isPresent()) {
                        EventSlot confirmedSlot = confirmedEvent.get().getSelectedSlots();
                        builder.isConfirmed(true)
                               .confirmedDate(confirmedSlot.getSlotDate())
                               .confirmedStartTime(confirmedSlot.getStartTime())
                               .confirmedEndTime(confirmedSlot.getEndTime());
                    } else {
                        builder.isConfirmed(false);
                    }
                    
                    return builder.build();
                })
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
