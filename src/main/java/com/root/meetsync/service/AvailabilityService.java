package com.root.meetsync.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.root.meetsync.dto.SlotCountDto;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.HostAvailability;
import com.root.meetsync.entity.ParticipantAvailability;
import com.root.meetsync.repository.HostAvailabilityRepository;
import com.root.meetsync.repository.ParticipantAvailabilityRepository;


@Service
public class AvailabilityService {
    @Autowired
    private ParticipantAvailabilityRepository participantAvailabilityRepository;

    @Autowired
    private HostAvailabilityRepository hostAvailabilityRepository;

    public void saveAvailability(String participantName, List<Long> slotIds, Long eventId) {

        if (participantAvailabilityRepository.existsByParticipantNameAndEventSlot_Event_Id(participantName, eventId)) {
            throw new IllegalArgumentException("Participant name '" + participantName + "' has already submitted availability for this event.");
        }

        for (Long slotId : slotIds) {
            ParticipantAvailability pa = new ParticipantAvailability();
            EventSlot slot = new EventSlot();
            slot.setId(slotId);
            pa.setEventSlot(slot);
            pa.setParticipantName(participantName);
            participantAvailabilityRepository.save(pa);
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

     public Map<LocalDate, List<SlotCountDto>> getHeatmapStat(Long eventId){
        List<SlotCountDto> slotCounts = participantAvailabilityRepository.countTotalAttendeesBySlot(eventId);

        return slotCounts.stream().collect(
            java.util.stream.Collectors.groupingBy(SlotCountDto::getSlotDate)
        );
    }

   public Map<String, List<String>> getHeatmapDataForGuestView(Long eventId) {
    // 1. Fetch both participants and hosts
    List<ParticipantAvailability> participants = participantAvailabilityRepository.findByEventSlot_Event_Id(eventId);
    List<HostAvailability> hosts = hostAvailabilityRepository.findByEventSlot_Event_Id(eventId);

    // 2. Create a unified stream of "Name + Slot ID" pairs
    // We map both to a common structure or simply process them into the same map
    
    Map<String, List<String>> heatmap = participants.stream()
        .collect(java.util.stream.Collectors.groupingBy(
            pa -> pa.getEventSlot().getId().toString(),
            java.util.stream.Collectors.mapping(
                ParticipantAvailability::getParticipantName,
                java.util.stream.Collectors.toList()
            )
        ));

    // 3. Merge host data into the existing map
    hosts.forEach(ha -> {
        String slotId = ha.getEventSlot().getId().toString();
        String hostName = ha.getHost().getName(); // Assuming User entity has a getName() method
        
        heatmap.computeIfAbsent(slotId, k -> new java.util.ArrayList<>())
               .add(hostName + " (Host)"); // Optional: Add a tag to distinguish hosts
    });

    return heatmap;
}
}
