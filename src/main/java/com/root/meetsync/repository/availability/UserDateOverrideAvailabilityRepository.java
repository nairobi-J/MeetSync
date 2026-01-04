package com.root.meetsync.repository.availability;

import com.root.meetsync.entity.availability.UserDateOverrideAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDateOverrideAvailabilityRepository extends JpaRepository<UserDateOverrideAvailability, UUID> {
    List<UserDateOverrideAvailability> findByUserId(UUID userId);
    Optional<UserDateOverrideAvailability> findByUserIdAndDate(UUID userId, LocalDate date);
    List<UserDateOverrideAvailability> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);
}
