package com.root.meetsync.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.client.util.DateTime;
import com.root.meetsync.dto.booking.BookingResponseDTO;
import com.root.meetsync.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;

@Service
public class GoogleCalendarServiceImpl {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    public void createGoogleEvent(User host, BookingResponseDTO booking) throws Exception {
        // 1. Validate tokens
        if (host.getOauthToken() == null || host.getOauthToken().getRefreshToken() == null) {
            System.out.println("Skip Calendar: No Refresh Token found for " + host.getEmail());
            return;
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
                .setDescription("Meeting confirmed via MeetSync with " + booking.getInviteeEmail());

        // Convert LocalDateTime to Google DateTime format
        DateTime start = new DateTime(Date.from(booking.getStartTime().atZone(ZoneId.systemDefault()).toInstant()));
        event.setStart(new EventDateTime().setDateTime(start).setTimeZone(host.getTimezone()));

        DateTime end = new DateTime(Date.from(booking.getEndTime().atZone(ZoneId.systemDefault()).toInstant()));
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone(host.getTimezone()));

        // 5. Push to Primary Calendar
        service.events().insert("primary", event).execute();
    }
}