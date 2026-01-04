package com.root.meetsync.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantAvailabilityDto {
  private UUID id;
    private UUID eventSlotId; 
    private UUID userId;
    private LocalDateTime createdAt;
}
