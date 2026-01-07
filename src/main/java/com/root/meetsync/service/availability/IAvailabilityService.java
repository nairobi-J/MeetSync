package com.root.meetsync.service.availability;

import com.root.meetsync.dto.availability.AvailableSlotDTO;
import com.root.meetsync.dto.availability.SetupAvailabilityRequest;

import java.util.List;


public interface IAvailabilityService {
    void setupAvailability(Long userId, SetupAvailabilityRequest request);
    List<AvailableSlotDTO> getAvailableSlots(String emailPrefix, String timezone);
    String getUserBookingLink(Long userId);
    SetupAvailabilityRequest getUserAvailability(Long userId);
}
