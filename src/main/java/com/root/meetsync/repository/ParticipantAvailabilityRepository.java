package com.root.meetsync.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.root.meetsync.entity.ParticipantAvailability;

@Repository
public interface ParticipantAvailabilityRepository extends JpaRepository<ParticipantAvailability, UUID>{

    
} 
