package com.root.meetsync.service.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.root.meetsync.dto.booking.BookingResponseDTO;
import com.root.meetsync.entity.ConfirmedEvent;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.User;

@Service
public class GoogleCalendarServiceImpl {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    // --- METHOD 1: For Heatmap/Event Confirmation (New) ---
    public String createGoogleEventFromHeatmap(ConfirmedEvent confirmed) throws Exception {
        User host = confirmed.getEvent().getHost();
        EventSlot slot = confirmed.getSelectedSlots();

        // 1. Convert LocalDate + LocalTime to ZonedDateTime
        ZoneId zoneId = ZoneId.of(host.getTimezone() != null ? host.getTimezone() : "UTC");

        ZonedDateTime startZoned = slot.getSlotDate().atTime(slot.getStartTime()).atZone(zoneId);
        ZonedDateTime endZoned = slot.getSlotDate().atTime(slot.getEndTime()).atZone(zoneId);

        // 2. Prepare Google Event Info
        String summary = "MeetSync: " + confirmed.getEvent().getTitle();
        String description = "Meeting finalized via MeetSync Heatmap.";

        // 3. Call the common helper to push to Google and return event ID
        return pushToGoogle(host, summary, description, startZoned, endZoned);
    }

    // --- METHOD 2: For 1-on-1 Availability (Your existing one) ---
      public String createGoogleEvent(User host, BookingResponseDTO booking) throws Exception {
        // 1. Validate tokens
        if (host.getOauthToken() == null || host.getOauthToken().getRefreshToken() == null) {
            System.out.println("Skip Calendar: No Refresh Token found for " + host.getEmail());
            return null;
        }

        // 2. Get fresh Access Token using Refresh Token
        GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                host.getOauthToken().getRefreshToken(),
                clientId,
                clientSecret
        ).execute();

        String accessToken = response.getAccessToken();

        // 3. Initialize Google Calendar Client
        Calendar service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                null
        )
                .setApplicationName("MeetSync")
                .setHttpRequestInitializer(request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
                .build();

        // 4. Build the Event
        Event event = new Event()
                .setSummary("MeetSync: " + booking.getInviteeName())
                .setDescription("Meeting confirmed via MeetSync with " + booking.getInviteeEmail())
                ;
           
        // add attendees
       event.setAttendees(java.util.Collections.singletonList(
               new com.google.api.services.calendar.model.EventAttendee().setEmail(booking.getInviteeEmail())
               ));
        // event reminder - minutes before
        event.setReminders(new Event.Reminders().setUseDefault(false).setOverrides(java.util.Collections.singletonList(
                new EventReminder().setMethod("popup").setMinutes(booking.getRemindersMinutesBefore())
        )));

        // Convert LocalDateTime to Google DateTime format
        DateTime start = new DateTime(Date.from(booking.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        event.setStart(new EventDateTime().setDateTime(start).setTimeZone(host.getTimezone()));

        DateTime end = new DateTime(Date.from(booking.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone(host.getTimezone()));

        // 5. Push to Primary Calendar and return event ID
        Event createdEvent = service.events().insert("primary", event).setSendUpdates("all").execute();
        return createdEvent.getId();
    }

    // --- PRIVATE HELPER: Handles the actual API handshake ---
    private String pushToGoogle(User host, String summary, String desc, ZonedDateTime start, ZonedDateTime end) throws Exception {
        if (host.getOauthToken() == null || host.getOauthToken().getRefreshToken() == null) {
            return null;
        }

        GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                new NetHttpTransport(), new GsonFactory(),
                host.getOauthToken().getRefreshToken(), clientId, clientSecret
        ).execute();

        Calendar service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), null
        )
                .setApplicationName("MeetSync")
                .setHttpRequestInitializer(req -> req.getHeaders().setAuthorization("Bearer " + response.getAccessToken()))
                .build();

        Event event = new Event().setSummary(summary).setDescription(desc);

        event.setStart(new EventDateTime()
                .setDateTime(new DateTime(Date.from(start.toInstant())))
                .setTimeZone(host.getTimezone()));

        event.setEnd(new EventDateTime()
                .setDateTime(new DateTime(Date.from(end.toInstant())))
                .setTimeZone(host.getTimezone()));

        Event createdEvent = service.events().insert("primary", event).execute();
        return createdEvent.getId(); // Return the Google Calendar event ID
    }

    // --- DELETE GOOGLE CALENDAR EVENT ---
    public boolean deleteGoogleEvent(User host, String googleEventId) {
        try {
            if (host.getOauthToken() == null || host.getOauthToken().getRefreshToken() == null) {
                System.out.println("Skip Calendar Delete: No Refresh Token found for " + host.getEmail());
                return false;
            }

            if (googleEventId == null || googleEventId.isEmpty()) {
                System.out.println("Skip Calendar Delete: No Google Event ID provided");
                return false;
            }

            GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    host.getOauthToken().getRefreshToken(), clientId, clientSecret
            ).execute();

            Calendar service = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), null
            )
                    .setApplicationName("MeetSync")
                    .setHttpRequestInitializer(req -> req.getHeaders().setAuthorization("Bearer " + response.getAccessToken()))
                    .build();

            // Delete the event from Google Calendar
            service.events().delete("primary", googleEventId).execute();
            System.out.println("Successfully deleted Google Calendar event: " + googleEventId);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to delete Google Calendar event " + googleEventId + ": " + e.getMessage());
            return false;
        }
    }

    // --- UPDATE GOOGLE CALENDAR EVENT ---
    public boolean updateGoogleEvent(User host, String googleEventId, ConfirmedEvent newConfirmed) {
        try {
            if (host.getOauthToken() == null || host.getOauthToken().getRefreshToken() == null) {
                System.out.println("Skip Calendar Update: No Refresh Token found for " + host.getEmail());
                return false;
            }

            if (googleEventId == null || googleEventId.isEmpty()) {
                System.out.println("Skip Calendar Update: No Google Event ID provided");
                return false;
            }

            GoogleTokenResponse response = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    host.getOauthToken().getRefreshToken(), clientId, clientSecret
            ).execute();

            Calendar service = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(), null
            )
                    .setApplicationName("MeetSync")
                    .setHttpRequestInitializer(req -> req.getHeaders().setAuthorization("Bearer " + response.getAccessToken()))
                    .build();

            // Get the existing event
            Event existingEvent = service.events().get("primary", googleEventId).execute();
            
            // Update with new time information
            EventSlot slot = newConfirmed.getSelectedSlots();
            ZoneId zoneId = ZoneId.of(host.getTimezone() != null ? host.getTimezone() : "UTC");
            
            ZonedDateTime startZoned = slot.getSlotDate().atTime(slot.getStartTime()).atZone(zoneId);
            ZonedDateTime endZoned = slot.getSlotDate().atTime(slot.getEndTime()).atZone(zoneId);

            existingEvent.setStart(new EventDateTime()
                    .setDateTime(new DateTime(Date.from(startZoned.toInstant())))
                    .setTimeZone(host.getTimezone()));

            existingEvent.setEnd(new EventDateTime()
                    .setDateTime(new DateTime(Date.from(endZoned.toInstant())))
                    .setTimeZone(host.getTimezone()));

            // Update the summary if needed
            existingEvent.setSummary("MeetSync: " + newConfirmed.getEvent().getTitle());
            existingEvent.setDescription("Meeting updated via MeetSync Heatmap.");

            // Update the event in Google Calendar
            service.events().update("primary", googleEventId, existingEvent).execute();
            System.out.println("Successfully updated Google Calendar event: " + googleEventId);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to update Google Calendar event " + googleEventId + ": " + e.getMessage());
            return false;
        }
    }

    public List<Event> getAllGoogleCalendarEvents(User user) throws Exception {

    // 1. Validate tokens
    if (user.getOauthToken() == null || user.getOauthToken().getRefreshToken() == null) {
        throw new IllegalStateException("No Google refresh token found for user: " + user.getEmail());
    }

    // 2. Get fresh Access Token
    GoogleTokenResponse response = new GoogleRefreshTokenRequest(
            new NetHttpTransport(),
            new GsonFactory(),
            user.getOauthToken().getRefreshToken(),
            clientId,
            clientSecret
    ).execute();

    String accessToken = response.getAccessToken();

    // 3. Initialize Google Calendar Client
    Calendar service = new Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            null
    )
            .setApplicationName("MeetSync")
            .setHttpRequestInitializer(
                    request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
            )
            .build();

    // 4. Fetch events from primary calendar
    com.google.api.services.calendar.model.Events events = service.events()
            .list("primary")
            .setMaxResults(50)                 
            .setSingleEvents(true)            
            .setOrderBy("startTime")
        //     .setTimeMin(new DateTime(System.currentTimeMillis())) // upcoming only
            .execute();

    // 5. Return event list
    return events.getItems();
}

}