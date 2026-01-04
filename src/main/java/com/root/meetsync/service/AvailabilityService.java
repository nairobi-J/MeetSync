package com.root.meetsync.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.root.meetsync.dto.SlotCountDto;
import com.root.meetsync.dto.UserResponseDto;
import com.root.meetsync.entity.EventSlot;
import com.root.meetsync.entity.ParticipantAvailability;
import com.root.meetsync.repository.ParticipantAvailabilityRepository;

@Service
public class AvailabilityService {
    @Autowired
    private ParticipantAvailabilityRepository participantAvailabilityRepository;

    public void saveAvailability(UserResponseDto user, List<EventSlot> slots) {
         List<ParticipantAvailability> availabilities = slots.stream().map(slot -> {
            ParticipantAvailability pa = new ParticipantAvailability();
            pa.setUser(null);
            pa.setEventSlot(slot);

            return pa;
        }).collect(Collectors.toList());
        participantAvailabilityRepository.saveAll(availabilities);
    }

     public List<SlotCountDto> getHeatmapStat(UUID eventId){
        return participantAvailabilityRepository.countParticipantsBySlot(eventId);
    }
}
