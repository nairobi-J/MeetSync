package com.root.meetsync.service.impl;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.root.meetsync.dto.EventForm;
import org.springframework.stereotype.Service;
import com.google.api.client.util.DateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import java.time.LocalDateTime;
import java.time.LocalTime;



@Service
public class GoogleCalendarService {

    // Store the service after OAuth login (per user ideally in session)
    public static Calendar service;

    public Events getEvents() throws Exception {
        if (service == null) throw new Exception("User not authenticated!");

        return service.events()
                .list("primary")
                .setMaxResults(50)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();
    }


    public Events getTodayEvents() throws Exception {
        if (service == null) throw new Exception("User not authenticated!");

        // Get today’s date in system default timezone
        LocalDate today = LocalDate.now();

        // Start of today (00:00)
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // End of today (23:59:59)
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        // Convert to Google DateTime
        DateTime timeMin = new DateTime(startOfDay);
        DateTime timeMax = new DateTime(endOfDay);

        // Fetch events
        return service.events()
                .list("primary")
                .setTimeMin(timeMin)
                .setTimeMax(timeMax)
                .setMaxResults(50)
                .setSingleEvents(true)
                .setOrderBy("startTime")
                .execute();
    }


    


//    Create Event
public Event createEvent(EventForm eventForm) throws Exception {
    Event event = new Event();

    // Set event title (summary)
    event.setSummary(eventForm.getSummary());

    // Set location if provided
    if (eventForm.getLocation() != null && !eventForm.getLocation().isEmpty()) {
        event.setLocation(eventForm.getLocation());
    }

    // Set description if provided
    if (eventForm.getDescription() != null && !eventForm.getDescription().isEmpty()) {
        event.setDescription(eventForm.getDescription());
    }

    // Parse the date
    LocalDate eventDate = LocalDate.parse(eventForm.getDate());

    // Set start and end times
    EventDateTime start = new EventDateTime();
    EventDateTime end = new EventDateTime();

    if (eventForm.isAllDay()) {
        // All-day event
        start.setDate(new DateTime(eventDate.toString()));
        end.setDate(new DateTime(eventDate.plusDays(1).toString()));
    } else {
        // Timed event
        LocalTime startTime = eventForm.getStartTime() != null && !eventForm.getStartTime().isEmpty()
                ? LocalTime.parse(eventForm.getStartTime())
                : LocalTime.of(9, 0); // Default 9:00 AM

        LocalTime endTime = eventForm.getEndTime() != null && !eventForm.getEndTime().isEmpty()
                ? LocalTime.parse(eventForm.getEndTime())
                : startTime.plusHours(1); // Default 1 hour duration

        LocalDateTime startDateTime = LocalDateTime.of(eventDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(eventDate, endTime);

        // Convert to DateTime with timezone
        ZoneId zoneId = ZoneId.of("Asia/Dhaka"); // Use your timezone
        Date startDate = Date.from(startDateTime.atZone(zoneId).toInstant());
        Date endDate = Date.from(endDateTime.atZone(zoneId).toInstant());

        start.setDateTime(new DateTime(startDate));
        start.setTimeZone("Asia/Dhaka");

        end.setDateTime(new DateTime(endDate));
        end.setTimeZone("Asia/Dhaka");
    }

    event.setStart(start);
    event.setEnd(end);

    // Insert event into calendar
    String calendarId = "primary";
    
    Event createdEvent = service.events().insert(calendarId, event).execute();

    System.out.println("Event created: " + createdEvent.getHtmlLink());

    return createdEvent;
}


}