package com.root.meetsync.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.util.List;
@Component
public class GoogleAuthService {

    public GoogleAuthorizationCodeFlow getFlow() throws Exception {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = GsonFactory.getDefaultInstance();

        var secrets = GoogleClientSecrets.load(
                jsonFactory,
                new InputStreamReader(getClass().getResourceAsStream("/GoogleCredentials.json"))
        );

        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                secrets,
                List.of(CalendarScopes.CALENDAR)
        )
                .setAccessType("offline")  // gets refresh token
                .setApprovalPrompt("force")
                .build();
    }
}