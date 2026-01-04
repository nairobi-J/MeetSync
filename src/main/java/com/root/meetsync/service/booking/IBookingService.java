package com.root.meetsync.service.booking;

import com.root.meetsync.dto.booking.BookingRequestDTO;
import com.root.meetsync.dto.booking.BookingResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IBookingService {
    BookingResponseDTO createBookingRequest(String emailPrefix, BookingRequestDTO request);
    List<BookingResponseDTO> getHostBookings(UUID hostId);
    BookingResponseDTO confirmBooking(UUID bookingId);
    BookingResponseDTO cancelBooking(UUID bookingId);
}
