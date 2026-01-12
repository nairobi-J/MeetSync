package com.root.meetsync.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.client.util.DateTime;
import com.root.meetsync.dto.booking.BookingResponseDTO;
import com.root.meetsync.entity.ConfirmedEvent;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Service
public class GoogleCalendarServiceImpl {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    // --- METHOD 1: For Heatmap/Event Confirmation (New) ---
    public void createGoogleEventFromHeatmap(ConfirmedEvent confirmed) throws Exception {
        User host = confirmed.getEvent().getHost();
        EventSlot slot = confirmed.getSelectedSlots();

        // 1. Convert LocalDate + LocalTime to ZonedDateTime
        ZoneId zoneId = ZoneId.of(host.getTimezone() != null ? host.getTimezone() : "UTC");

        ZonedDateTime startZoned = slot.getSlotDate().atTime(slot.getStartTime()).atZone(zoneId);
        ZonedDateTime endZoned = slot.getSlotDate().atTime(slot.getEndTime()).atZone(zoneId);

        // 2. Prepare Google Event Info
        String summary = "MeetSync: " + confirmed.getEvent().getTitle();
        String description = "Meeting finalized via MeetSync Heatmap.";

        // 3. Call the common helper to push to Google
        pushToGoogle(host, summary, description, startZoned, endZoned);
    }

    // --- METHOD 2: For 1-on-1 Availability (Your existing one) ---
    public void createGoogleEvent(User host, BookingResponseDTO booking) throws Exception {
        ZoneId zoneId = ZoneId.of(host.getTimezone() != null ? host.getTimezone() : "UTC");

        ZonedDateTime startZoned = booking.getStartTime().atZone(zoneId);
        ZonedDateTime endZoned = booking.getEndTime().atZone(zoneId);

        String summary = "MeetSync: " + booking.getInviteeName();
        String description = "Confirmed with " + booking.getInviteeEmail();

        pushToGoogle(host, summary, description, startZoned, endZoned);
    }

    // --- PRIVATE HELPER: Handles the actual API handshake ---
    private void pushToGoogle(User host, String summary, String desc, ZonedDateTime start, ZonedDateTime end) throws Exception {
        if (host.getOauthToken() == null || host.getOauthToken().getRefreshToken() == null) {
            return;
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

        service.events().insert("primary", event).execute();
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
            .setTimeMin(new DateTime(System.currentTimeMillis())) // upcoming only
            .execute();

    // 5. Return event list
    return events.getItems();
}

}