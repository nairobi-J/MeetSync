package com.root.meetsync.repository;

import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGoogleId(String googleId);

    Optional<User> findByEmail(String email);

    @Query("""
                SELECT u FROM User u
                WHERE SUBSTRING(u.email, 1, LOCATE('@', u.email) - 1) = :prefix
            """)
    Optional<User> findByExactEmailPrefix(@Param("prefix") String prefix);
    
    // Admin panel methods
    List<User> findByStatus(UserStatus status);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
