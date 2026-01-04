package com.root.meetsync.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.root.meetsync.dto.SlotCountDto;
import com.root.meetsync.entity.ParticipantAvailability;

@Repository
public interface ParticipantAvailabilityRepository extends JpaRepository<ParticipantAvailability, UUID>{
   @Query("SELECT new com.root.meetsync.dto.SlotCountDto(es.id, es.startTime, COUNT(pa.id)) " +
       "FROM EventSlot es " +
       "LEFT JOIN ParticipantAvailability pa ON pa.eventSlot = es " +
       "WHERE es.event.id = :eventId " +
       "GROUP BY es.id, es.startTime " +
       "ORDER BY es.startTime ASC")
   List<SlotCountDto> countParticipantsBySlot(@Param("eventId") UUID eventId);
} 
