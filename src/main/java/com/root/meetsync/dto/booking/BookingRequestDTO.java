package com.root.meetsync.dto.booking;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class BookingRequestDTO {
    private String inviteeName;
    private String inviteeEmail;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private String timezone;
    
    // Helper method to get full datetime
    public LocalDateTime getStartTime() {
        if (bookingDate != null && bookingTime != null) {
            return LocalDateTime.of(bookingDate, bookingTime);
        }
        return null;
    }
}
