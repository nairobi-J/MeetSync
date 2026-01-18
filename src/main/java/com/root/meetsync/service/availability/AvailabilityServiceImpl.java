package com.root.meetsync.service.availability;

import com.root.meetsync.dto.availability.*;
import com.root.meetsync.entity.*;
import com.root.meetsync.entity.availability.UserAvailability;
import com.root.meetsync.entity.availability.UserDateOverrideAvailability;
import com.root.meetsync.entity.availability.UserMeetingPreference;
import com.root.meetsync.entity.booking.Booking;
import com.root.meetsync.entity.booking.BookingStatus;
import com.root.meetsync.repository.*;
import com.root.meetsync.repository.availability.UserAvailabilityRepository;
import com.root.meetsync.repository.availability.UserDateOverrideAvailabilityRepository;
import com.root.meetsync.repository.availability.UserMeetingPreferenceRepository;
import com.root.meetsync.repository.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements IAvailabilityService {

    private final UserRepository userRepository;
    private final UserMeetingPreferenceRepository preferenceRepository;
    private final UserAvailabilityRepository availabilityRepository;
    private final UserDateOverrideAvailabilityRepository overrideRepository;
    private final BookingRepository bookingRepository;

    @Value("${meetsync.base-url:https://base_url_missing}")
    private String bookingBaseUrl;

    @Transactional
    public void setupAvailability(Long userId, SetupAvailabilityRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Setup or update meeting preferences
        UserMeetingPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(new UserMeetingPreference());
        preference.setUser(user);
        preference.setMeetingDurationMinutes(request.getMeetingDurationMinutes());
        preference.setMinNoticeHours(request.getMinNoticeHours());
        preference.setFutureDaysAllowed(request.getFutureDaysAllowed());
        preference.setBufferTimeMinutes(request.getBufferTimeMinutes() != null ? request.getBufferTimeMinutes() : 0);
        preference.setTimezone(request.getTimezone());
        preferenceRepository.save(preference);

        // Clear existing weekly availability
        availabilityRepository.deleteAll(availabilityRepository.findByUserId(userId));

        // Setup new weekly availability
        if (request.getWeeklyAvailability() != null) {
            for (AvailabilityDTO dto : request.getWeeklyAvailability()) {
                UserAvailability availability = new UserAvailability();
                availability.setUser(user);
                availability.setDayOfWeek(dto.getDayOfWeek());
                availability.setStartTime(dto.getStartTime());
                availability.setEndTime(dto.getEndTime());
                availabilityRepository.save(availability);
            }
        }

        // Clear existing date overrides
        overrideRepository.deleteAll(overrideRepository.findByUserId(userId));

        // Setup date overrides if provided
        if (request.getDateOverrides() != null) {
            for (DateOverrideDTO dto : request.getDateOverrides()) {
                UserDateOverrideAvailability override = new UserDateOverrideAvailability();
                override.setUser(user);
                override.setDate(dto.getDate());
                override.setStartTime(dto.getStartTime());
                override.setEndTime(dto.getEndTime());
                override.setUnavailable(dto.getUnavailable());
                overrideRepository.save(override);
            }
        }



        
    }

    public SetupAvailabilityRequest getUserAvailability(Long userId) {
        SetupAvailabilityRequest request = new SetupAvailabilityRequest();
        
        // Fetch user's meeting preferences
        UserMeetingPreference preference = preferenceRepository.findByUserId(userId).orElse(null);
        if (preference != null) {
            request.setMeetingDurationMinutes(preference.getMeetingDurationMinutes());
            request.setMinNoticeHours(preference.getMinNoticeHours());
            request.setFutureDaysAllowed(preference.getFutureDaysAllowed());
            request.setBufferTimeMinutes(preference.getBufferTimeMinutes());
            request.setTimezone(preference.getTimezone());
        }
        
        // Fetch weekly availability
        List<UserAvailability> weeklyAvailability = availabilityRepository.findByUserId(userId);
        if (!weeklyAvailability.isEmpty()) {
            List<AvailabilityDTO> weeklyDTOs = new ArrayList<>();
            for (UserAvailability ua : weeklyAvailability) {
                AvailabilityDTO dto = new AvailabilityDTO();
                dto.setDayOfWeek(ua.getDayOfWeek());
                dto.setStartTime(ua.getStartTime());
                dto.setEndTime(ua.getEndTime());
                weeklyDTOs.add(dto);
            }
            request.setWeeklyAvailability(weeklyDTOs);
        }
        
        // Fetch date overrides
        List<UserDateOverrideAvailability> overrides = overrideRepository.findByUserId(userId);
        if (!overrides.isEmpty()) {
            List<DateOverrideDTO> overrideDTOs = new ArrayList<>();
            for (UserDateOverrideAvailability override : overrides) {
                DateOverrideDTO dto = new DateOverrideDTO();
                dto.setDate(override.getDate());
                dto.setStartTime(override.getStartTime());
                dto.setEndTime(override.getEndTime());
                dto.setUnavailable(override.getUnavailable());
                overrideDTOs.add(dto);
            }
            request.setDateOverrides(overrideDTOs);
        }
        
        return request;
    }
  

    public List<AvailableSlotDTO> getAvailableSlots(String emailPrefix, String timezone) {
        User user = userRepository.findByEmail(emailPrefix)
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().startsWith(emailPrefix + "@"))
                        .findFirst())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserMeetingPreference preference = preferenceRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("User has not set up availability"));

        List<UserAvailability> weeklyAvailability = availabilityRepository.findByUserId(user.getId());
        List<UserDateOverrideAvailability> dateOverrides = overrideRepository.findByUserId(user.getId());
        List<Booking> existingBookings = bookingRepository.findByHostId(user.getId());

        // Use invitee's timezone if provided, otherwise use host's timezone
        String displayTimezone = (timezone != null && !timezone.isEmpty()) ? timezone : preference.getTimezone();

        return generateAvailableSlots(preference, weeklyAvailability, dateOverrides, existingBookings, displayTimezone);
    }

    private List<AvailableSlotDTO> generateAvailableSlots(
            UserMeetingPreference preference,
            List<UserAvailability> weeklyAvailability,
            List<UserDateOverrideAvailability> dateOverrides,
            List<Booking> existingBookings,
            String displayTimezone) {

        List<AvailableSlotDTO> slots = new ArrayList<>();
        ZoneId hostTimezone = ZoneId.of(preference.getTimezone());
        ZoneId inviteeTimezone = ZoneId.of(displayTimezone);
        LocalDateTime now = LocalDateTime.now(hostTimezone);
        LocalDateTime minBookingTime = now.plusHours(preference.getMinNoticeHours());

        for (int i = 0; i <= preference.getFutureDaysAllowed(); i++) {
            LocalDate date = LocalDate.now(hostTimezone).plusDays(i);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            // Check for date override
            UserDateOverrideAvailability override = dateOverrides.stream()
                    .filter(o -> o.getDate().equals(date))
                    .findFirst()
                    .orElse(null);

            LocalTime startTime;
            LocalTime endTime;

            if (override != null) {
                if (Boolean.TRUE.equals(override.getUnavailable())) {
                    continue; // Skip unavailable days
                }
                startTime = override.getStartTime();
                endTime = override.getEndTime();
            } else {
                // Use weekly availability
                UserAvailability dayAvailability = weeklyAvailability.stream()
                        .filter(a -> a.getDayOfWeek() == dayOfWeek)
                        .findFirst()
                        .orElse(null);

                if (dayAvailability == null) {
                    continue; // No availability for this day
                }

                startTime = dayAvailability.getStartTime();
                endTime = dayAvailability.getEndTime();
            }

            // Handle time ranges that cross midnight
            boolean crossesMidnight = endTime.isBefore(startTime) || endTime.equals(startTime);
            LocalDate endDate = date;
            if (crossesMidnight) {
                endDate = date.plusDays(1);
            }

            LocalDateTime slotStart = LocalDateTime.of(date, startTime);
            LocalDateTime slotEndBoundary = LocalDateTime.of(endDate, endTime);
            int slotDuration = preference.getMeetingDurationMinutes();
            int bufferTime = preference.getBufferTimeMinutes();
            int totalSlotTime = slotDuration + bufferTime;

            while (slotStart.plusMinutes(slotDuration).isBefore(slotEndBoundary)
                    || slotStart.plusMinutes(slotDuration).equals(slotEndBoundary)) {

                final LocalDateTime currentSlotStart = slotStart;
                LocalDateTime slotEnd = currentSlotStart.plusMinutes(slotDuration);

                // Check if slot is in the future with minimum notice
                boolean isOverride = (override != null);
                if ((isOverride && !Boolean.TRUE.equals(override.getUnavailable())) || currentSlotStart.isAfter(minBookingTime)) {
                    // Check if slot is not already booked
                    boolean isBooked = false;
                    for (Booking b : existingBookings) {
                        if (b.getStatus() != BookingStatus.CANCELLED &&
                                (currentSlotStart.isBefore(b.getEndTime()) && slotEnd.isAfter(b.getStartTime()))) {
                            isBooked = true;
                            break;
                        }
                    }

                    if (!isBooked) {
                        // Convert to invitee's timezone if different
                        LocalDateTime displayStart = currentSlotStart;
                        LocalDateTime displayEnd = slotEnd;

                        if (!hostTimezone.equals(inviteeTimezone)) {
                            ZonedDateTime zonedStart = currentSlotStart.atZone(hostTimezone);
                            ZonedDateTime zonedEnd = slotEnd.atZone(hostTimezone);
                            displayStart = zonedStart.withZoneSameInstant(inviteeTimezone).toLocalDateTime();
                            displayEnd = zonedEnd.withZoneSameInstant(inviteeTimezone).toLocalDateTime();
                        }

                        AvailableSlotDTO slot = new AvailableSlotDTO();
                        slot.setStartTime(displayStart);
                        slot.setEndTime(displayEnd);
                        slot.setTimezone(displayTimezone);
                        slots.add(slot);
                    }
                }

                slotStart = slotStart.plusMinutes(totalSlotTime);
            }
        }

        return slots;
    }

    public String getUserBookingLink(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new RuntimeException("User email is invalid");
        }
        
        String emailPrefix = user.getEmail().substring(0, user.getEmail().indexOf("@"));
        String base = bookingBaseUrl != null ? bookingBaseUrl.trim() : "https:base_url_missing";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/u/" + emailPrefix;
    }

   public UserMeetingPreference getUserMeetingPreference(Long userId) {
        UserMeetingPreference preference = preferenceRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User has not set up availability"));
        return preference;
    }


}
