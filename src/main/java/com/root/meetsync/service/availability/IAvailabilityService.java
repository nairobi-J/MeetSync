package com.root.meetsync.service.availability;

import com.root.meetsync.dto.availability.AvailableSlotDTO;
import com.root.meetsync.dto.availability.SetupAvailabilityRequest;
import com.root.meetsync.entity.availability.UserMeetingPreference;

import java.util.List;


public interface IAvailabilityService {
    void setupAvailability(Long userId, SetupAvailabilityRequest request);
    List<AvailableSlotDTO> getAvailableSlots(String emailPrefix, String timezone);
    String getUserBookingLink(Long userId);
    SetupAvailabilityRequest getUserAvailability(Long userId);
    UserMeetingPreference getUserMeetingPreference(Long userId);
   
}
