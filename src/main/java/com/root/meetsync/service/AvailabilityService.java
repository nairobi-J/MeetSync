package com.root.meetsync.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional
    public void saveAvailability(String participantName, List<Long> slotIds, Long eventId) {
        // Check if participant has already submitted - if so, update their availability
        if (participantAvailabilityRepository.existsByParticipantNameAndEventSlot_Event_Id(participantName, eventId)) {
            // Delete existing availability for this participant and event
            participantAvailabilityRepository.deleteByParticipantNameAndEventSlot_Event_Id(participantName, eventId);
        }

        // Save new availability selections
        for (Long slotId : slotIds) {
            ParticipantAvailability pa = new ParticipantAvailability();
            EventSlot slot = new EventSlot();
            slot.setId(slotId);
            pa.setEventSlot(slot);
            pa.setParticipantName(participantName);
            participantAvailabilityRepository.save(pa);
        }
    }

     public Map<LocalDate, List<SlotCountDto>> getHeatmapStat(Long eventId){
        List<SlotCountDto> slotCounts = participantAvailabilityRepository.countTotalAttendeesBySlot(eventId);

        return slotCounts.stream().collect(
            java.util.stream.Collectors.groupingBy(SlotCountDto::getSlotDate)
        );
    }

   public Map<String, List<String>> getHeatmapDataForGuestView(Long eventId) {
   
    List<ParticipantAvailability> participants = participantAvailabilityRepository.findByEventSlot_Event_Id(eventId);
    List<HostAvailability> hosts = hostAvailabilityRepository.findByEventSlot_Event_Id(eventId);
    
    Map<String, List<String>> heatmap = participants.stream()
        .collect(java.util.stream.Collectors.groupingBy(
            pa -> pa.getEventSlot().getId().toString(),
            java.util.stream.Collectors.mapping(
                ParticipantAvailability::getParticipantName,
                java.util.stream.Collectors.toList()
            )
        ));

    hosts.forEach(ha -> {
        String slotId = ha.getEventSlot().getId().toString();
        String hostName = ha.getHost().getName(); 
        
        heatmap.computeIfAbsent(slotId, k -> new java.util.ArrayList<>())
               .add(hostName + " (Host)"); 
    });

    return heatmap;
}
}
