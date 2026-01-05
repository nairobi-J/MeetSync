package com.root.meetsync.repository.availability;

import com.root.meetsync.entity.availability.UserAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;


@Repository
public interface UserAvailabilityRepository extends JpaRepository<UserAvailability, Long> {
    List<UserAvailability> findByUserId(Long userId);
    List<UserAvailability> findByUserIdAndDayOfWeek(Long userId, DayOfWeek dayOfWeek);
}
