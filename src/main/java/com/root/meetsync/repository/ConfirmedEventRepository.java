package com.root.meetsync.repository;

import com.root.meetsync.entity.ConfirmedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmedEventRepository extends JpaRepository<ConfirmedEvent, Long> {

    Optional<ConfirmedEvent> findByEvent_Id(Long eventId);
    void deleteByEvent_Id(Long eventId);
}
