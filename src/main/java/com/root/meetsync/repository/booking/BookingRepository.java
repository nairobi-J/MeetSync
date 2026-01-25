package com.root.meetsync.repository.booking;

import com.root.meetsync.entity.User;
import com.root.meetsync.entity.booking.Booking;
import com.root.meetsync.entity.booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByHostId(Long hostId);
    List<Booking> findByHostIdAndStatus(Long hostId, BookingStatus status);
    List<Booking> findByHostIdAndStartTimeBetween(Long hostId, LocalDateTime start, LocalDateTime end);
    
    // Find bookings by host entity
    List<Booking> findByHost(User host);
    
    // Find active bookings (PENDING or CONFIRMED) for a host
    @Query("SELECT b FROM Booking b WHERE b.host = :host AND (b.status = 'PENDING' OR b.status = 'CONFIRMED')")
    List<Booking> findActiveBookingsByHost(@Param("host") User host);
    
    // Find bookings with Google Calendar event IDs for a host
    @Query("SELECT b FROM Booking b WHERE b.host = :host AND b.googleCalendarEventId IS NOT NULL")
    List<Booking> findByHostAndGoogleCalendarEventIdIsNotNull(@Param("host") User host);
    
    // Find future active bookings for a host
    @Query("SELECT b FROM Booking b WHERE b.host = :host AND b.status IN ('PENDING', 'CONFIRMED') AND b.startTime > :now")
    List<Booking> findFutureActiveBookingsByHost(@Param("host") User host, @Param("now") LocalDateTime now);
}
