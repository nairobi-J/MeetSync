package com.root.meetsync.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class CreateEventRequest {
    
    @NotBlank(message = "Event title is required")
    @Size(min = 3, max = 100, message = "Event title must be between 3 and 100 characters")
    private String title;
    
    @NotBlank(message = "Timezone is required")
    private String timezone;
    
    @NotNull(message = "Slot duration is required")
    private Integer slotDuration;

    @NotNull(message = "Earliest time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime earliestTime;

    @NotNull(message = "Latest time is required")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime latestTime;

    @NotEmpty(message = "At least one date must be selected")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private List<LocalDate> selectedDates;
}