package com.root.meetsync.dto.event;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventListDTO {
    private Long id;
    private String title;
    private String timezone;
    private String shareLink;
    private LocalTime earliestTime;
    private LocalTime latestTime;
    private Integer slotDuration;
    
    // Confirmed event details
    private boolean isConfirmed;
    private LocalDate confirmedDate;
    private LocalTime confirmedStartTime;
    private LocalTime confirmedEndTime;
}
