package com.root.meetsync.repository.booking;

import com.root.meetsync.entity.booking.Booking;
import com.root.meetsync.entity.booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByHostId(Long hostId);
    List<Booking> findByHostIdAndStatus(Long hostId, BookingStatus status);
    List<Booking> findByHostIdAndStartTimeBetween(Long hostId, LocalDateTime start, LocalDateTime end);
}
