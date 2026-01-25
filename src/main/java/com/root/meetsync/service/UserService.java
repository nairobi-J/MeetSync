package com.root.meetsync.service;

import com.root.meetsync.dto.UserRegistrationDto;
import com.root.meetsync.entity.DeletionReason;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserDeletion;
import com.root.meetsync.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User processOAuthUser(OAuth2AuthenticationToken authentication);
    User registerNewUser(UserRegistrationDto registrationDto);
    void updateProfile(Long userId, String name, String timezone  );

    // Basic user methods
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailExcludeDeleted(String email);
    void updatePassword(String email, String rawPassword);
    User getUserByEmailPrefix(String prefix);
    User getUserByEmailPrefixExcludeDeleted(String prefix);
    
    // Admin panel methods
    List<User> findByStatus(UserStatus status);
    List<User> findAllUsers();
    List<User> findAllUsersExcludeDeleted();
    Page<User> findByStatusPaginated(UserStatus status, Pageable pageable);
    Page<User> findAllUsersPaginated(Pageable pageable);
    void approveUser(Long userId);
    void rejectUser(Long userId, String reason);
    
    // Account deletion methods
    UserDeletion deleteOwnAccount(Long userId, DeletionReason reason, String notes);
    UserDeletion deleteUserByAdmin(Long userId, Long adminId, DeletionReason reason, String notes);
}