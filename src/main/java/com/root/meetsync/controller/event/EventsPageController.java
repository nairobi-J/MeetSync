package com.root.meetsync.controller.event;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.dto.event.EventCardDTO;
import com.root.meetsync.service.event.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventsPageController {

    private final EventService eventService;

    @GetMapping("/testevents")
    public String eventsPage(
            @ModelAttribute("currentUser") CurrentUserDTO currentUser,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "month") String timeRange,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            Model model
    ) {

        YearMonth ym = (month != null && year != null)
                ? YearMonth.of(year, month)
                : YearMonth.now();

        model.addAttribute("currentMonth", ym.getMonthValue());
        model.addAttribute("currentYear", ym.getYear());
        model.addAttribute("currentMonthName", ym.getMonth().name());

        List<EventCardDTO> events = switch (filter.toLowerCase()) {
            case "scheduled", "expired", "pending" ->
                    eventService.getEventsByStatus(currentUser, filter);
            default ->
                    eventService.getAllEventsByUser(currentUser);
        };

        events = switch (timeRange.toLowerCase()) {
            case "today" -> eventService.getEventsForToday(currentUser);
            case "week" -> eventService.getEventsForWeek(currentUser);
            case "month" -> eventService.getEventsForMonth(
                    currentUser, ym.getYear(), ym.getMonthValue());
            default -> events;
        };

        model.addAttribute("events", events);
        model.addAttribute("hasEvents", !events.isEmpty());
        model.addAttribute("selectedFilter", filter);
        model.addAttribute("selectedTimeRange", timeRange);

        return "events/EventsPage";
    }

    @GetMapping("/events/view")
    public String viewEventDetails(
            @RequestParam("id") Long eventId,
            @ModelAttribute("currentUser") CurrentUserDTO currentUser,
            Model model
    ) {
        model.addAttribute(
                "event",
                eventService.getEventById(eventId, currentUser)
        );
        return "events/EventDetailsPage";
    }
}
