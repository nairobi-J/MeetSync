package com.root.meetsync.controller.booking;

import com.root.meetsync.dto.booking.BookingRequestDTO;
import com.root.meetsync.dto.booking.BookingResponseDTO;
import com.root.meetsync.service.booking.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final IBookingService bookingService;

    @PostMapping("/u/{emailPrefix}")
    public ResponseEntity<BookingResponseDTO> createBooking(
            @PathVariable String emailPrefix,
            @RequestBody BookingRequestDTO request) {
        BookingResponseDTO response = bookingService.createBookingRequest(emailPrefix, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/host")
    public ResponseEntity<List<BookingResponseDTO>> getHostBookings(@RequestHeader("User-Id") UUID userId) {
        List<BookingResponseDTO> bookings = bookingService.getHostBookings(userId);
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponseDTO> confirmBooking(@PathVariable UUID bookingId) {
        BookingResponseDTO response = bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponseDTO> cancelBooking(@PathVariable UUID bookingId) {
        BookingResponseDTO response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }
}
