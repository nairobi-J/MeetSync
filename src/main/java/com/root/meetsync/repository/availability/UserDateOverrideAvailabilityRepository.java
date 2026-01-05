package com.root.meetsync.repository.availability;

import com.root.meetsync.entity.availability.UserDateOverrideAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDateOverrideAvailabilityRepository extends JpaRepository<UserDateOverrideAvailability, Long> {
    List<UserDateOverrideAvailability> findByUserId(Long userId);
    Optional<UserDateOverrideAvailability> findByUserIdAndDate(Long userId, LocalDate date);
    List<UserDateOverrideAvailability> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
