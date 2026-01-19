package com.root.meetsync.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false)
    private String title;

    private String timezone;
    private LocalTime earliestTime;
    private LocalTime latestTime;
    private int slotDuration =60;

    @Column(unique = true)
    private String shareLink;

    // Google Calendar Integration
    private String googleCalendarEventId;
    private String googleCalendarSyncStatus = "PENDING"; // PENDING, SYNCED, FAILED, DELETED
    private LocalDateTime lastSyncTimestamp;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<EventSlot> slots;


}
