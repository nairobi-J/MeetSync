package com.root.meetsync.repository.event;

import com.root.meetsync.dto.event.EventCardDTO;
import com.root.meetsync.entity.Event;
import com.root.meetsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventsPageRepository extends JpaRepository<Event, Long> {

//    // Find all events by host
//    public List<EventCardDTO> getAllEventsByUser(User user);


    // Find events by host ordered by creation date
    List<Event> findByHostOrderByCreatedAtDesc(User host);

    Optional<Event> findByIdAndHostId(Long id, Long hostId);


    // Find events with slots in date range
    @Query("SELECT DISTINCT e FROM Event e " +
            "JOIN e.slots s " +
            "WHERE e.host = :host " +
            "AND s.slotDate >= :startDate " +
            "AND s.slotDate <= :endDate " +
            "ORDER BY e.createdAt DESC")
    List<Event> findByHostAndDateRange(
            @Param("host") User host,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find events with future slots (Scheduled)
    @Query("SELECT DISTINCT e FROM Event e " +
            "JOIN e.slots s " +
            "WHERE e.host = :host " +
            "AND s.slotDate >= :currentDate " +
            "ORDER BY e.createdAt DESC")
    List<Event> findScheduledEvents(
            @Param("host") User host,
            @Param("currentDate") LocalDate currentDate
    );

    // Find events with only past slots (Expired)
    @Query("SELECT DISTINCT e FROM Event e " +
            "WHERE e.host = :host " +
            "AND e.id NOT IN (" +
            "    SELECT DISTINCT e2.id FROM Event e2 " +
            "    JOIN e2.slots s2 " +
            "    WHERE e2.host = :host " +
            "    AND s2.slotDate >= :currentDate" +
            ") " +
            "AND EXISTS (" +
            "    SELECT 1 FROM EventSlot s3 " +
            "    WHERE s3.event = e" +
            ") " +
            "ORDER BY e.createdAt DESC")
    List<Event> findExpiredEvents(
            @Param("host") User host,
            @Param("currentDate") LocalDate currentDate
    );

    // Find events without slots (Pending/Not Scheduled)
    @Query("SELECT e FROM Event e " +
            "WHERE e.host = :host " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM EventSlot s " +
            "    WHERE s.event = e" +
            ") " +
            "ORDER BY e.createdAt DESC")
    List<Event> findPendingEvents(@Param("host") User host);
}