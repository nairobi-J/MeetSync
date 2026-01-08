package com.root.meetsync.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SlotCountDto {
    private Long slotId;
    private LocalTime startTime;
    private LocalDate slotDate;
    private Long participantCount;
    
}
