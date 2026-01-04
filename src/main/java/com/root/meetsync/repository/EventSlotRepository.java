package com.root.meetsync.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.root.meetsync.dto.SlotCountDto;
import com.root.meetsync.entity.EventSlot;

@Repository
public interface EventSlotRepository extends JpaRepository<EventSlot, UUID> {

    @Query("SELECT new com.root.meetsync.dto.SlotCountDto(pa.id, pa.startTime, COUNT(pa.id)) " +
           "FROM ParticipantAvailability pa " +
           "pa.evenSlot.event.id = :eventId " +
           "WHERE es.event.id = :eventId " +
           "GROUP BY es.id, es.startTime")
    List<SlotCountDto> countParticipantsBySlot(@Param("eventId") UUID eventId);

}
