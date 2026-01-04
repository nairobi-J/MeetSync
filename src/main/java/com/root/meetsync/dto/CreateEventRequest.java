package com.root.meetsync.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
@Getter
@Setter
public class CreateEventRequest {
    private String title;
    private String timezone;
    private LocalTime earliestTime;
    private LocalTime latestTime;
    private List<SlotRequest> selectedSlots;

    @Getter
    @Setter
    public static class SlotRequest {
        private LocalDate date;
        private LocalTime start;
        private LocalTime end;
    }
}
