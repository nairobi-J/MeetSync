package com.root.meetsync.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "host_availability")
@Getter
@Setter
public class HostAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne
    @JoinColumn(name = "event_slot_id", nullable = false)
    private EventSlot eventSlot;

    private LocalDateTime createdAt = LocalDateTime.now();


}