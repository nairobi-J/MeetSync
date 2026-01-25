package com.root.meetsync.repository;

import com.root.meetsync.entity.UserDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeletionRepository extends JpaRepository<UserDeletion, Long> {
    
    Optional<UserDeletion> findByUserId(Long userId);
    
    List<UserDeletion> findByDeletedBy(Long deletedBy);
    
    @Query("SELECT ud FROM UserDeletion ud WHERE ud.deletedAt >= :startDate")
    List<UserDeletion> findDeletionsAfter(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT ud FROM UserDeletion ud WHERE ud.googleCalendarCleanupAttempted = false")
    List<UserDeletion> findPendingGoogleCalendarCleanup();
    
    @Query("SELECT COUNT(ud) FROM UserDeletion ud WHERE ud.deletedAt >= :startDate")
    Long countDeletionsAfter(@Param("startDate") LocalDateTime startDate);
}
