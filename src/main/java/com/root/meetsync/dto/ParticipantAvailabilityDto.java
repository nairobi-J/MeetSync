package com.root.meetsync.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantAvailabilityDto {
  private Long id;
    private Long eventSlotId; 
    String participantName;
    private LocalDateTime createdAt;
}
