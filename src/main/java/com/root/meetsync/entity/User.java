package com.root.meetsync.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.root.meetsync.entity.availability.UserAvailability;
import com.root.meetsync.entity.availability.UserDateOverrideAvailability;
import com.root.meetsync.entity.availability.UserMeetingPreference;
import com.root.meetsync.entity.booking.Booking;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "google_id", nullable = true, unique = true)
    private String googleId;

    private String name;
    private String email;
    private String timezone;
    private String password;

    @Column(unique = true)
    private String username; // For booking link: /u/{username}

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private UserOAuthToken oauthToken;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private UserMeetingPreference meetingPreference;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<UserAvailability> availabilities = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<UserDateOverrideAvailability> dateOverrides = new ArrayList<>();

    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Booking> bookings = new ArrayList<>();
}