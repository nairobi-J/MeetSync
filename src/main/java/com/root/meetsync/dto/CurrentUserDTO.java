package com.root.meetsync.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // This replaces the need for the static factory
public class CurrentUserDTO {
    private Long id;
    private String name;
    private String email;
    private String googleId;
    private String profilePic;
}