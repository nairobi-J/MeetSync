package com.root.meetsync.repository;

import com.root.meetsync.entity.HostAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface  HostAvailabilityRepository extends JpaRepository<HostAvailability, UUID> {

}
