package com.root.meetsync.dto.availability;

import lombok.Data;

import java.util.List;

@Data
public class SetupAvailabilityRequest {
    private Integer meetingDurationMinutes;
    private Integer minNoticeHours;
    private Integer futureDaysAllowed;
    private Integer bufferTimeMinutes; // Buffer time before each meeting
    private String timezone;
    private List<AvailabilityDTO> weeklyAvailability;
    private List<DateOverrideDTO> dateOverrides;
}
