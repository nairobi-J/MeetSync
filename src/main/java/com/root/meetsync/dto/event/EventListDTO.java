package com.root.meetsync.dto.event;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventListDTO {
    private Long id;
    private String title;
    private String timezone;
}
