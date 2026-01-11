package com.root.meetsync.service.booking;

import com.root.meetsync.dto.booking.BookingRequestDTO;
import com.root.meetsync.dto.booking.BookingResponseDTO;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.availability.UserAvailability;
import com.root.meetsync.entity.availability.UserDateOverrideAvailability;
import com.root.meetsync.entity.availability.UserMeetingPreference;
import com.root.meetsync.entity.booking.Booking;
import com.root.meetsync.entity.booking.BookingStatus;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.repository.availability.UserAvailabilityRepository;
import com.root.meetsync.repository.availability.UserDateOverrideAvailabilityRepository;
import com.root.meetsync.repository.availability.UserMeetingPreferenceRepository;
import com.root.meetsync.repository.booking.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final UserMeetingPreferenceRepository preferenceRepository;
    private final UserAvailabilityRepository userAvailabilityRepository;
    private final UserDateOverrideAvailabilityRepository userDateOverrideAvailabilityRepository;

    @Transactional
    public BookingResponseDTO createBookingRequest(String emailPrefix, BookingRequestDTO request) {
        String email = emailPrefix + "@%";
        User host = userRepository.findByEmail(emailPrefix)
                .or(() -> userRepository.findAll().stream()
                        .filter(u -> u.getEmail() != null && u.getEmail().startsWith(emailPrefix + "@"))
                        .findFirst())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserMeetingPreference preference = preferenceRepository.findByUserId(host.getId())
                .orElseThrow(() -> new RuntimeException("User has not set up availability"));

        // Get the start time from date and time
        LocalDateTime startTime = request.getStartTime();
        if (startTime == null) {
            throw new RuntimeException("Booking date and time are required");
        }
        
        // Validate the slot is available
        LocalDateTime endTime = startTime.plusMinutes(preference.getMeetingDurationMinutes());

        // Check for conflicts
        List<Booking> conflicts = bookingRepository.findByHostIdAndStartTimeBetween(
                host.getId(),
                startTime.minusMinutes(preference.getMeetingDurationMinutes()),
                startTime.plusMinutes(preference.getMeetingDurationMinutes())
        );

        boolean hasConflict = conflicts.stream()
                .anyMatch(b -> b.getStatus() != BookingStatus.CANCELLED);

        if (hasConflict) {
            throw new RuntimeException("This time slot is no longer available");
        }

        // Validate that the requested time is within host's actual availability
        validateHostAvailability(host.getId(), startTime, endTime, preference);

        // Create booking
        Booking booking = new Booking();
        booking.setHost(host);
        booking.setInviteeName(request.getInviteeName());
        booking.setInviteeEmail(request.getInviteeEmail());
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setRemindersMinutesBefore(preference.getMinNoticeHours() * 60); // set reminders based on min notice
        booking.setTimezone(request.getTimezone());
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        return mapToResponseDTO(saved);
    }

    public List<BookingResponseDTO> getHostBookings(Long hostId) {
        return bookingRepository.findByHostId(hostId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponseDTO confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        return mapToResponseDTO(saved);
    }

    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        return mapToResponseDTO(saved);
    }

    private BookingResponseDTO mapToResponseDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setHostName(booking.getHost().getName());
        dto.setHostEmail(booking.getHost().getEmail());
        dto.setInviteeName(booking.getInviteeName());
        dto.setInviteeEmail(booking.getInviteeEmail());
        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setRemindersMinutesBefore(booking.getRemindersMinutesBefore());
        dto.setTimezone(booking.getTimezone());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }

    private void validateHostAvailability(Long hostId, LocalDateTime requestedStart, LocalDateTime requestedEnd, UserMeetingPreference preference) {
        // Get host's availability settings
        List<UserAvailability> weeklyAvailability = userAvailabilityRepository.findByUserId(hostId);
        List<UserDateOverrideAvailability> dateOverrides = userDateOverrideAvailabilityRepository.findByUserId(hostId);
        
        LocalDate requestedDate = requestedStart.toLocalDate();
        DayOfWeek requestedDayOfWeek = requestedDate.getDayOfWeek();
        LocalTime requestedTime = requestedStart.toLocalTime();
        
        // Check for date override first
        UserDateOverrideAvailability override = dateOverrides.stream()
                .filter(o -> o.getDate().equals(requestedDate))
                .findFirst()
                .orElse(null);
        
        if (override != null) {
            if (Boolean.TRUE.equals(override.getUnavailable())) {
                throw new RuntimeException("Host is not available on this date");
            }
            // Check if requested time is within override availability
            if (requestedTime.isBefore(override.getStartTime()) || requestedEnd.toLocalTime().isAfter(override.getEndTime())) {
                throw new RuntimeException("Requested time is outside host's available hours for this date");
            }
        } else {
            // Check weekly availability
            UserAvailability dayAvailability = weeklyAvailability.stream()
                    .filter(a -> a.getDayOfWeek() == requestedDayOfWeek)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Host is not available on this day of the week"));
            
            // Check if requested time is within weekly availability
            if (requestedTime.isBefore(dayAvailability.getStartTime()) || requestedEnd.toLocalTime().isAfter(dayAvailability.getEndTime())) {
                throw new RuntimeException("Requested time is outside host's available hours");
            }
        }
        
        // Validate minimum notice
        ZoneId hostTimezone = ZoneId.of(preference.getTimezone());
        LocalDateTime now = LocalDateTime.now(hostTimezone);
        LocalDateTime minBookingTime = now.plusHours(preference.getMinNoticeHours());
        
        if (requestedStart.isBefore(minBookingTime)) {
            throw new RuntimeException("Booking requires at least " + preference.getMinNoticeHours() + " hours notice");
        }
        
        // Validate future days limit
        LocalDate maxDate = LocalDate.now(hostTimezone).plusDays(preference.getFutureDaysAllowed());
        if (requestedDate.isAfter(maxDate)) {
            throw new RuntimeException("Booking date exceeds the allowed future booking limit");
        }
    }
}
