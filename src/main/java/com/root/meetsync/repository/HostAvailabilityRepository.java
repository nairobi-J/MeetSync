package com.root.meetsync.repository;

import com.root.meetsync.entity.HostAvailability;
import com.root.meetsync.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface  HostAvailabilityRepository extends JpaRepository<HostAvailability, Long> {
    List<HostAvailability> findByEventSlot_Event_Id( Long eventId);


    Optional<HostAvailability> findByHostAndEventSlot_Id(User host, Long slotId);

    @Modifying
    @Transactional
    void deleteByHostAndEventSlot_Event_Id(User host, Long eventId);

}
