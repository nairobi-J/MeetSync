package com.root.meetsync.service.impl;

import com.root.meetsync.entity.*;
import com.root.meetsync.repository.UserDeletionRepository;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.repository.EventRepository;
import com.root.meetsync.service.AccountDeletionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AccountDeletionServiceImpl implements AccountDeletionService {

    private final UserRepository userRepository;
    private final UserDeletionRepository userDeletionRepository;
    private final EventRepository eventRepository;
    private final GoogleCalendarServiceImpl googleCalendarService;
    private final SecureRandom secureRandom;

    public AccountDeletionServiceImpl(
            UserRepository userRepository,
            UserDeletionRepository userDeletionRepository,
            EventRepository eventRepository,
            GoogleCalendarServiceImpl googleCalendarService) {
        this.userRepository = userRepository;
        this.userDeletionRepository = userDeletionRepository;
        this.eventRepository = eventRepository;
        this.googleCalendarService = googleCalendarService;
        this.secureRandom = new SecureRandom();
    }

    @Override
    public UserDeletion deleteUserAccount(User user, DeletionReason reason, String notes) {
        return performUserDeletion(user, user, reason, notes);
    }

    @Override
    public UserDeletion deleteUserAccountByAdmin(User user, User admin, DeletionReason reason, String notes) {
        return performUserDeletion(user, admin, reason, notes);
    }

    private UserDeletion performUserDeletion(User userToDelete, User deletedBy, DeletionReason reason, String notes) {
        // Check if user is already deleted
        if (userToDelete.getStatus() == UserStatus.DELETED) {
            throw new IllegalStateException("User is already deleted");
        }

        // Create deletion record first
        UserDeletion deletion;
        if (userToDelete.equals(deletedBy)) {
            deletion = new UserDeletion(userToDelete, reason, notes);
        } else {
            deletion = new UserDeletion(userToDelete, deletedBy, reason, notes);
        }
        
        // Save deletion record
        deletion = userDeletionRepository.save(deletion);

        // Update user status to DELETED
        userToDelete.setStatus(UserStatus.DELETED);
        
        // Anonymize user data
        anonymizeUserData(userToDelete);
        
        // Save anonymized user
        userRepository.save(userToDelete);

        // Attempt Google Calendar cleanup
        boolean cleanupSuccess = attemptGoogleCalendarCleanup(deletion);
        
        // Update deletion record with cleanup status
        deletion.setGoogleCalendarCleanupAttempted(true);
        deletion.setGoogleCalendarCleanupSuccessful(cleanupSuccess);
        userDeletionRepository.save(deletion);

        return deletion;
    }

    @Override
    public void anonymizeUserData(User user) {
        // Generate random identifiers
        String randomId = generateRandomString(10);
        
        // Anonymize personal data
        user.setName("Deleted User " + randomId);
        user.setEmail("deleted_" + randomId + "@deleted.local");
        
        // Clear sensitive data
        user.setOauthToken(null);
        user.setTimezone(null);
        user.setProfilePic(null);
        
        // Note: Events remain but are now associated with anonymized user
    }

    @Override
    public boolean attemptGoogleCalendarCleanup(UserDeletion deletion) {
        try {
            // Find the user before anonymization by looking up the deletion record
            Optional<User> userOpt = userRepository.findById(deletion.getUserId());
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            // If user has no OAuth token, skip cleanup
            if (user.getOauthToken() == null) {
                return true; // Consider this successful since there's nothing to clean
            }

            // Get all events created by this user that have Google Calendar IDs
            List<Event> userEvents = eventRepository.findByHostAndGoogleCalendarEventIdIsNotNull(user);
            
            boolean allDeleted = true;
            for (Event event : userEvents) {
                try {
                    boolean deleted = googleCalendarService.deleteGoogleEvent(user, event.getGoogleCalendarEventId());
                    if (!deleted) {
                        allDeleted = false;
                    } else {
                        // Clear the Google Calendar ID from the event
                        event.setGoogleCalendarEventId(null);
                        eventRepository.save(event);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to delete Google Calendar event " + event.getGoogleCalendarEventId() + 
                                     " for user " + deletion.getOriginalEmail() + ": " + e.getMessage());
                    allDeleted = false;
                }
            }

            return allDeleted;
        } catch (Exception e) {
            System.err.println("Google Calendar cleanup failed for user " + deletion.getOriginalEmail() + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isUserDeleted(Long userId) {
        return userDeletionRepository.findByUserId(userId).isPresent();
    }

    @Override
    public UserDeletion getDeletionRecord(Long userId) {
        return userDeletionRepository.findByUserId(userId).orElse(null);
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return result.toString();
    }
}
