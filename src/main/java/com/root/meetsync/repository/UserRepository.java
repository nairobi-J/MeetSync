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
    
    // Find non-deleted users
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.status != 'DELETED'")
    Optional<User> findByEmailExcludeDeleted(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.googleId = :googleId AND u.status != 'DELETED'")
    Optional<User> findByGoogleIdExcludeDeleted(@Param("googleId") String googleId);

    @Query("""
                SELECT u FROM User u
                WHERE SUBSTRING(u.email, 1, LOCATE('@', u.email) - 1) = :prefix
            """)
    Optional<User> findByExactEmailPrefix(@Param("prefix") String prefix);
    
    @Query("""
                SELECT u FROM User u
                WHERE SUBSTRING(u.email, 1, LOCATE('@', u.email) - 1) = :prefix
                AND u.status != 'DELETED'
            """)
    Optional<User> findByExactEmailPrefixExcludeDeleted(@Param("prefix") String prefix);
    
    // Admin panel methods
    List<User> findByStatus(UserStatus status);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    // Get all users excluding deleted ones (for admin)
    @Query("SELECT u FROM User u WHERE u.status != 'DELETED'")
    List<User> findAllExcludeDeleted();
    
    @Query("SELECT u FROM User u WHERE u.status != 'DELETED'")
    Page<User> findAllExcludeDeleted(Pageable pageable);
    
    // Find users by status excluding deleted (for admin filtering)
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.status != 'DELETED'")
    List<User> findByStatusExcludeDeleted(@Param("status") UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.status != 'DELETED'")
    Page<User> findByStatusExcludeDeleted(@Param("status") UserStatus status, Pageable pageable);
}
