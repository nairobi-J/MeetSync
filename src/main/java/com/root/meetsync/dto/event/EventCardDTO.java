package com.root.meetsync.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCardDTO {
    private Long id;
    private String title;
    private String status; // "Scheduled", "Pending", "Expired"
    private LocalDate scheduleDate; // For scheduled events
    private String scheduleTime; // Formatted time string
    private String duration; // e.g., "60 min"
    private boolean hasSlots; // To determine if "Not Scheduled" should be shown
    private String videoCallIcon; // For display purposes
}