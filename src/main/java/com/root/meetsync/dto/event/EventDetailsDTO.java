package com.root.meetsync.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventDetailsDTO {
    private Long id;
    private String title;
    private String timezone;
    private LocalTime earliestTime;
    private LocalTime latestTime;
    private int slotDuration;
    private String shareLink;
    private LocalDateTime createdAt;
}

