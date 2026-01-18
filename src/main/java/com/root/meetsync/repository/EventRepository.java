
package com.root.meetsync.repository;

import java.util.Optional;

import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByShareLink(String shareLink);
    
    // Check if an event with the same title exists for a specific user
    boolean existsByTitleAndHost(String title, User host);
}
