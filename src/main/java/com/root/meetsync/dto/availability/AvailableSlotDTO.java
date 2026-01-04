package com.root.meetsync.dto.availability;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AvailableSlotDTO {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String timezone; // The timezone these times are displayed in
}
