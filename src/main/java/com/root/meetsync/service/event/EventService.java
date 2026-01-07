package com.root.meetsync.service.event;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.dto.event.EventCardDTO;
import com.root.meetsync.dto.event.EventResponseDTO;
import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.User;
import com.root.meetsync.repository.event.EventsPageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventsPageRepository eventsRepository;
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a");

    /* ---------- INTERNAL HELPER ---------- */
    private User userFrom(CurrentUserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        return user;
    }

    /* ---------- LIST PAGES ---------- */

    @Transactional(readOnly = true)
    public List<EventCardDTO> getAllEventsByUser(CurrentUserDTO currentUser) {
        return eventsRepository
                .findByHostOrderByCreatedAtDesc(userFrom(currentUser))
                .stream()
                .map(this::convertToCardDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventCardDTO> getEventsByStatus(CurrentUserDTO currentUser, String status) {

        User user = userFrom(currentUser);
        LocalDate today = LocalDate.now();
        List<Event> events;

        switch (status.toLowerCase()) {
            case "scheduled" -> events = eventsRepository.findScheduledEvents(user, today);
            case "expired" -> events = eventsRepository.findExpiredEvents(user, today);
            case "pending" -> events = eventsRepository.findPendingEvents(user);
            default -> events = eventsRepository.findByHostOrderByCreatedAtDesc(user);
        }

        return events.stream()
                .map(this::convertToCardDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventCardDTO> getEventsForToday(CurrentUserDTO user) {
        LocalDate today = LocalDate.now();
        return getEventsForMonth(user, today.getYear(), today.getMonthValue());
    }

    @Transactional(readOnly = true)
    public List<EventCardDTO> getEventsForWeek(CurrentUserDTO user) {
        LocalDate start = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate end = start.plusDays(6);

        return eventsRepository
                .findByHostAndDateRange(userFrom(user), start, end)
                .stream()
                .map(this::convertToCardDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventCardDTO> getEventsForMonth(CurrentUserDTO user, int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        return eventsRepository
                .findByHostAndDateRange(userFrom(user), start, end)
                .stream()
                .map(this::convertToCardDTO)
                .collect(Collectors.toList());
    }

    /* ---------- SINGLE EVENT (SECURE) ---------- */

    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long eventId, CurrentUserDTO currentUser) {
        return eventsRepository
                .findByIdAndHostId(eventId, currentUser.getId())
                .map(this::convertToResponseDTO)
                .orElseThrow(() ->
                        new AccessDeniedException("Event not found"));
    }

    /* ---------- MAPPERS ---------- */

    private EventCardDTO convertToCardDTO(Event event) {
        EventSlot slot = getEarliestSlot(event);

        return EventCardDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .status(determineEventStatus(event))
                .hasSlots(slot != null)
                .scheduleDate(slot != null ? slot.getSlotDate() : null)
                .scheduleTime(slot != null
                        ? formatTimeRange(slot.getStartTime(), slot.getEndTime())
                        : "Not Scheduled")
                .duration(event.getSlotDuration() + " min")
                .build();
    }

    private EventResponseDTO convertToResponseDTO(Event event) {
        EventSlot earliest = getEarliestSlot(event);

        return EventResponseDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .status(determineEventStatus(event))
                .shareLink(event.getShareLink())
                .earliestSlotDate(earliest != null ? earliest.getSlotDate() : null)
                .earliestTime(event.getEarliestTime())
                .latestTime(event.getLatestTime())
                .build();
    }

    private String determineEventStatus(Event event) {
        if (event.getSlots() == null || event.getSlots().isEmpty()) return "Pending";

        LocalDate today = LocalDate.now();
        return event.getSlots().stream()
                .anyMatch(s -> !s.getSlotDate().isBefore(today))
                ? "Scheduled"
                : "Expired";
    }

    private EventSlot getEarliestSlot(Event event) {
        return event.getSlots() == null ? null :
                event.getSlots().stream()
                        .min(Comparator.comparing(EventSlot::getSlotDate)
                                .thenComparing(EventSlot::getStartTime))
                        .orElse(null);
    }

    private String formatTimeRange(java.time.LocalTime start, java.time.LocalTime end) {
        return start.format(TIME_FORMATTER) + " - " + end.format(TIME_FORMATTER);
    }
}
