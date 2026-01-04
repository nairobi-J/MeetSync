package com.root.meetsync.dto.booking;

import com.root.meetsync.entity.booking.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingResponseDTO {
    private UUID id;
    private String hostName;
    private String hostEmail;
    private String inviteeName;
    private String inviteeEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
