package com.root.meetsync.repository;

import java.util.List;
import java.util.Optional;

import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByShareLink(String shareLink);
    
    // Check if an event with the same title exists for a specific user
    boolean existsByTitleAndHost(String title, User host);
    
    // Find events by host
    List<Event> findByHost(User host);
    
    // Find events by host that have Google Calendar event IDs
    @Query("SELECT e FROM Event e WHERE e.host = :host AND e.googleCalendarEventId IS NOT NULL")
    List<Event> findByHostAndGoogleCalendarEventIdIsNotNull(User host);
}
