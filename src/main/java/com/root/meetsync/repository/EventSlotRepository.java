package com.root.meetsync.repository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.root.meetsync.entity.EventSlot;

@Repository
public interface EventSlotRepository extends JpaRepository<EventSlot, UUID> {

   List<EventSlot> findByEventId(UUID eventId);
}
