package com.root.meetsync.repository.availability;

import com.root.meetsync.entity.availability.UserMeetingPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMeetingPreferenceRepository extends JpaRepository<UserMeetingPreference, UUID> {
    Optional<UserMeetingPreference> findByUserId(UUID userId);
}
