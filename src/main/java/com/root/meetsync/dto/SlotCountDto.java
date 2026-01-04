package com.root.meetsync.dto;

import java.time.LocalTime;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlotCountDto {
    private UUID slotId;
    private LocalTime startTime;
    private Long participantCount;
}
