package com.root.meetsync.repository;


import com.root.meetsync.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository <Event, UUID>{
}
