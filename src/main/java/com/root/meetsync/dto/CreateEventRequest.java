package com.root.meetsync.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class CreateEventRequest {
    private String title;
    private String timezone;
    private Integer slotDuration;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime earliestTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime latestTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> selectedDates;
}