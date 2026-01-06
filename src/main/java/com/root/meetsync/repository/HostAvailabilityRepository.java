package com.root.meetsync.repository;

import com.root.meetsync.entity.HostAvailability;
import com.root.meetsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface  HostAvailabilityRepository extends JpaRepository<HostAvailability, Long> {
    List<HostAvailability> findByEventSlot_Event_Id(Long eventId);
    @Transactional
    void deleteByHostAndEventSlot_Event_Id(User user, Long eventId);

}
