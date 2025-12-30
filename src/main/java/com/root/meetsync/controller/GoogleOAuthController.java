package com.root.meetsync.controller;


import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.client.auth.oauth2.Credential;
import com.root.meetsync.service.impl.GoogleAuthService;
import com.root.meetsync.service.impl.GoogleCalendarService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GoogleOAuthController {

    private final GoogleAuthService authService;

    public GoogleOAuthController(GoogleAuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/google-login")
    public String login() throws Exception {
        var flow = authService.getFlow();
        var url = flow.newAuthorizationUrl()
                .setRedirectUri("http://localhost:8080/oauth2/callback")
                .build();
        return "redirect:" + url;
    }

    @GetMapping("/oauth2/callback")
    public String callback(@RequestParam("code") String code) throws Exception {
        var flow = authService.getFlow();
        var token = flow.newTokenRequest(code)
                .setRedirectUri("http://localhost:8080/oauth2/callback")
                .execute();

        Credential credential = flow.createAndStoreCredential(token, "user");

        GoogleCalendarService.service = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        ).setApplicationName("MeetSync").build(); // store service for user session

        return "redirect:/events/today";
    }
}