package com.root.meetsync.service.availability;

import com.root.meetsync.dto.availability.AvailableSlotDTO;
import com.root.meetsync.dto.availability.SetupAvailabilityRequest;

import java.util.List;
import java.util.UUID;

public interface IAvailabilityService {
    void setupAvailability(UUID userId, SetupAvailabilityRequest request);
    List<AvailableSlotDTO> getAvailableSlots(String emailPrefix, String timezone);
    String getUserBookingLink(UUID userId);
}
