package com.root.meetsync.dto.booking;

import com.root.meetsync.entity.User;
import com.root.meetsync.entity.booking.BookingStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingResponseDTO {
    private Long id;
    private String hostName;
    private String hostEmail;
    private String inviteeName;
    private String inviteeEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer remindersMinutesBefore;
    private String timezone;
    private BookingStatus status;
    private LocalDateTime createdAt;
}
