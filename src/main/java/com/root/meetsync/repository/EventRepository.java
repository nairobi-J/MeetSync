package com.root.meetsync.repository;


import com.root.meetsync.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository <Event, Long>{

    Optional<Event> findByShareLink(String shareLink);
}
