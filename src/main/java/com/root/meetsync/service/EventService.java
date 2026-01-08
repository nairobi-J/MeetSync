package com.root.meetsync.service;

import com.root.meetsync.dto.CreateEventRequest;
import com.root.meetsync.entity.Event;
import org.springframework.security.core.Authentication; // Updated import
import org.springframework.stereotype.Service;

@Service
public interface EventService {
    // Changed OAuth2AuthenticationToken to Authentication
    Event createEvent(CreateEventRequest request, Authentication auth);
}