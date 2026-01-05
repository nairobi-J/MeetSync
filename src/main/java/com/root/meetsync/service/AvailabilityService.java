package com.root.meetsync.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.root.meetsync.dto.SlotCountDto;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.ParticipantAvailability;
import com.root.meetsync.entity.User;
import com.root.meetsync.repository.ParticipantAvailabilityRepository;
import com.root.meetsync.repository.UserRepository;

@Service
public class AvailabilityService {
    @Autowired
    private ParticipantAvailabilityRepository participantAvailabilityRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveAvailability(UUID userId, List<UUID> slotIds) {

         User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            for (UUID slotId : slotIds) {
                ParticipantAvailability pa = new ParticipantAvailability();
                EventSlot slot = new EventSlot();
                slot.setId(slotId);
                pa.setEventSlot(slot);

                pa.setUser(user);
                participantAvailabilityRepository.save(pa);
            }
        }
    }

//     public void saveAvailability(UUID userId, List<UUID> slotIds) {
//     // 1. Fetch user once
//     User user = userRepository.findById(userId)
//         .orElseThrow(() -> new RuntimeException("User not found"));

//     // 2. Map slot IDs to Availability entities
//     List<ParticipantAvailability> availabilities = slotIds.stream()
//         .map(slotId -> {
//             ParticipantAvailability pa = new ParticipantAvailability();
            
//             // Using a reference to avoid a DB hit for every EventSlot
//             EventSlot slot = new EventSlot();
//             slot.setId(slotId);
            
//             pa.setEventSlot(slot);
//             pa.setUser(user);
//             return pa;
//         })
//         .collect(Collectors.toList());

//     // 3. Perform a single batch save
//     participantAvailabilityRepository.saveAll(availabilities);
// }

     public Map<LocalDate, List<SlotCountDto>> getHeatmapStat(UUID eventId){
        List<SlotCountDto> slotCounts = participantAvailabilityRepository.countParticipantsBySlot(eventId);

        return slotCounts.stream().collect(
            java.util.stream.Collectors.groupingBy(SlotCountDto::getSlotDate)
        );
    }
}
