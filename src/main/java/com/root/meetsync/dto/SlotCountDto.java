package com.root.meetsync.dto;

import java.time.LocalTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SlotCountDto {
    private UUID slotId;
    private LocalTime startTime;
    private Long participantCount;
}
