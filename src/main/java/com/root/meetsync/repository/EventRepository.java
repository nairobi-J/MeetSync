
package com.root.meetsync.repository;


import java.util.Optional;

import com.root.meetsync.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import com.root.meetsync.entity.Event;

public interface EventRepository extends JpaRepository <Event, Long>{

    Optional<Event> findByShareLink(String shareLink);
}
