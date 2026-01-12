package com.root.meetsync.service.booking;

import com.root.meetsync.dto.booking.BookingRequestDTO;
import com.root.meetsync.dto.booking.BookingResponseDTO;

import java.util.List;


public interface IBookingService {
    BookingResponseDTO createBookingRequest(String emailPrefix, BookingRequestDTO request);
    List<BookingResponseDTO> getHostBookings(Long hostId);
    BookingResponseDTO confirmBooking(Long bookingId);
    BookingResponseDTO cancelBooking(Long bookingId);
}
