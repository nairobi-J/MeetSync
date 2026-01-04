package com.root.meetsync.entity.availability;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.root.meetsync.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_meeting_preferences")
@Getter
@Setter
public class UserMeetingPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Column(name = "meeting_duration_minutes")
    private Integer meetingDurationMinutes = 30; // Default 30 minutes

    @Column(name = "min_notice_hours")
    private Integer minNoticeHours = 4; // Default 4 hours notice

    @Column(name = "future_days_allowed")
    private Integer futureDaysAllowed = 60; // Default 60 days

    @Column(name = "buffer_time_minutes")
    private Integer bufferTimeMinutes = 0; // Default 0 minutes buffer before each slot

    private String timezone = "Asia/Dhaka"; // Default timezone
}
