package com.root.meetsync.service;

import com.root.meetsync.entity.DeletionReason;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserDeletion;
import com.root.meetsync.entity.UserStatus;
import com.root.meetsync.entity.UserRole;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.repository.UserDeletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to test the user deletion functionality.
 * This can be used during development to verify the deletion system works correctly.
 */
@Service
@Transactional
public class UserDeletionTestService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserDeletionRepository userDeletionRepository;

    public UserDeletionTestService(
            UserService userService,
            UserRepository userRepository,
            UserDeletionRepository userDeletionRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.userDeletionRepository = userDeletionRepository;
    }

    /**
     * Create a test user for deletion testing
     */
    public User createTestUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(UserRole.USER);
        user.setTimezone("UTC");
        return userRepository.save(user);
    }

    /**
     * Test self-deletion
     */
    public UserDeletion testSelfDeletion(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Test user not found: " + email));
        
        return userService.deleteOwnAccount(user.getId(), DeletionReason.SELF_REQUESTED, "Testing self deletion");
    }

    /**
     * Test admin deletion
     */
    public UserDeletion testAdminDeletion(String userEmail, String adminEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Test user not found: " + userEmail));
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin user not found: " + adminEmail));
        
        return userService.deleteUserByAdmin(user.getId(), admin.getId(), 
                DeletionReason.ADMIN_INITIATED, "Testing admin deletion");
    }

    /**
     * Check if user is properly deleted and anonymized
     */
    public boolean verifyUserDeletion(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        UserDeletion deletion = userDeletionRepository.findByUserId(userId)
                .orElse(null);
        
        // Check status
        boolean statusDeleted = UserStatus.DELETED.equals(user.getStatus());
        
        // Check anonymization
        boolean nameAnonymized = user.getName().startsWith("Deleted User");
        boolean emailAnonymized = user.getEmail().endsWith("@deleted.local");
        boolean tokenCleared = user.getOauthToken() == null;
        
        // Check deletion record exists
        boolean deletionRecordExists = deletion != null;
        
        System.out.println("User Deletion Verification for ID " + userId + ":");
        System.out.println("- Status DELETED: " + statusDeleted);
        System.out.println("- Name anonymized: " + nameAnonymized + " (" + user.getName() + ")");
        System.out.println("- Email anonymized: " + emailAnonymized + " (" + user.getEmail() + ")");
        System.out.println("- OAuth token cleared: " + tokenCleared);
        System.out.println("- Deletion record exists: " + deletionRecordExists);
        
        if (deletion != null) {
            System.out.println("- Original email: " + deletion.getOriginalEmail());
            System.out.println("- Deletion reason: " + deletion.getReason());
            System.out.println("- Deleted at: " + deletion.getDeletedAt());
        }
        
        return statusDeleted && nameAnonymized && emailAnonymized && tokenCleared && deletionRecordExists;
    }

    /**
     * Clean up test data
     */
    public void cleanupTestUser(Long userId) {
        // Remove deletion record first
        userDeletionRepository.findByUserId(userId).ifPresent(userDeletionRepository::delete);
        
        // Remove user
        userRepository.deleteById(userId);
        
        System.out.println("Test user " + userId + " and deletion record cleaned up");
    }
}
