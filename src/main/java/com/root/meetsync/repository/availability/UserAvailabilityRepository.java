package com.root.meetsync.repository.availability;

import com.root.meetsync.entity.availability.UserAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserAvailabilityRepository extends JpaRepository<UserAvailability, UUID> {
    List<UserAvailability> findByUserId(UUID userId);
    List<UserAvailability> findByUserIdAndDayOfWeek(UUID userId, DayOfWeek dayOfWeek);
}
