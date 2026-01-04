package com.root.meetsync.repository.booking;

import com.root.meetsync.entity.booking.Booking;
import com.root.meetsync.entity.booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByHostId(UUID hostId);
    List<Booking> findByHostIdAndStatus(UUID hostId, BookingStatus status);
    List<Booking> findByHostIdAndStartTimeBetween(UUID hostId, LocalDateTime start, LocalDateTime end);
}
