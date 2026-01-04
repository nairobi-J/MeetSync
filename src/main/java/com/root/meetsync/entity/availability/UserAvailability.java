package com.root.meetsync.entity.availability;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.root.meetsync.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "user_availability")
@Getter
@Setter
public class UserAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime; // e.g., 09:00

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;   // e.g., 17:00
}
