package com.root.meetsync.service;

import java.util.List;
import java.util.UUID;
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

     public List<SlotCountDto> getHeatmapStat(UUID eventId){
        return participantAvailabilityRepository.countParticipantsBySlot(eventId);
    }
}
