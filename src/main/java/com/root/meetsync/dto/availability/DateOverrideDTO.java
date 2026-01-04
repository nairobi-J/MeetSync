package com.root.meetsync.dto.availability;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DateOverrideDTO {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean unavailable;
}
