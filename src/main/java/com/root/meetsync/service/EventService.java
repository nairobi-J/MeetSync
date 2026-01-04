package com.root.meetsync.service;

import com.root.meetsync.dto.CreateEventRequest;
import com.root.meetsync.entity.Event;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public interface EventService {
    Event createEvent(CreateEventRequest request, OAuth2AuthenticationToken auth);
}