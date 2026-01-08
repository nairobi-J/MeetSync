package com.root.meetsync.dto;

import lombok.*;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrentUserDTO {
    private Long id;
    private String name;
    private String email;
    private String googleId;
    private String profilePic;
    private String timezone;
}