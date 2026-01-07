package com.root.meetsync.repository;
import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.root.meetsync.dto.SlotCountDto;
import com.root.meetsync.entity.ParticipantAvailability;

@Repository
public interface ParticipantAvailabilityRepository extends JpaRepository<ParticipantAvailability, Long>{
   @Query("SELECT new com.root.meetsync.dto.SlotCountDto(es.id, es.startTime, es.slotDate, COUNT(pa.participantName)) " +
       "FROM EventSlot es " +
       "LEFT JOIN ParticipantAvailability pa ON pa.eventSlot = es " +
       "WHERE es.event.id = :eventId " +
       "GROUP BY es.id, es.startTime, es.slotDate " +
       "ORDER BY es.startTime ASC, es.slotDate ASC")
    List<SlotCountDto> countParticipantsBySlot(@Param("eventId") Long eventId);
    
    // all participant availabilities for a specific event
    @Query("SELECT pa FROM ParticipantAvailability pa WHERE pa.eventSlot.event.id = :eventId")
    List<ParticipantAvailability> findByEventSlot_Event_Id(@Param("eventId") Long eventId);

}
