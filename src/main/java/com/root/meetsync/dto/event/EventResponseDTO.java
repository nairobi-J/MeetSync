package com.root.meetsync.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDTO {
    private Long id;
    private String title;
    private String status; // "Scheduled", "Pending", "Expired"
    private String shareLink;
    private List<EventSlotDTO> slots;
    private int totalSlots;
    private LocalDate earliestSlotDate;
    private LocalTime earliestTime;
    private LocalTime latestTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventSlotDTO {
        private Long id;
        private LocalDate slotDate;
        private LocalTime startTime;
        private LocalTime endTime;
    }
}