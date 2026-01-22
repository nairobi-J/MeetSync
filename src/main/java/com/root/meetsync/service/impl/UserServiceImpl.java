package com.root.meetsync.service.impl;

import com.root.meetsync.dto.UserRegistrationDto;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserOAuthToken;
import com.root.meetsync.entity.UserStatus;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.service.AccessControlService;
import com.root.meetsync.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final PasswordEncoder passwordEncoder;
    private final AccessControlService accessControlService;

    public UserServiceImpl(UserRepository userRepository, OAuth2AuthorizedClientService authorizedClientService, PasswordEncoder passwordEncoder, AccessControlService accessControlService) {
        this.userRepository = userRepository;
        this.authorizedClientService = authorizedClientService;
        this.passwordEncoder = passwordEncoder;
        this.accessControlService = accessControlService;
    }


    @Override
    @Transactional
    public User registerNewUser(UserRegistrationDto dto) {
        // Check if email already exists
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setTimezone("UTC");


        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // google_id remains null for manual users
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void updatePassword(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }


    @Override
    @Transactional
    public User processOAuthUser(OAuth2AuthenticationToken authentication) {
        // 1. Get User Profile info
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        // 2. Get the OAuth Tokens (Access & Refresh)
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null) {
            throw new RuntimeException("OAuth2AuthorizedClient is null. User may need to re-authenticate.");
        }

        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = (client.getRefreshToken() != null) ? client.getRefreshToken().getTokenValue() : null;
        LocalDateTime expiresAt = LocalDateTime.ofInstant(client.getAccessToken().getExpiresAt(), ZoneId.systemDefault());

        // 3. Find or Create User
        User user = userRepository.findByGoogleId(googleId).orElse(null);
        boolean isNewUser = (user == null);
        
        if (isNewUser) {
            user = new User();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setName(name);
            user.setTimezone("UTC");
            user.setProfilePic(picture);
            
            // Set status and role for new users only
            user.setStatus(accessControlService.determineUserStatus(email));
            user.setRole(accessControlService.determineUserRole(email));
        } else {
            // Update existing user info
            user.setName(name);
            user.setProfilePic(picture);
        }

        // 4. Update or Create the Token record
        UserOAuthToken tokenEntity = user.getOauthToken();
        if (tokenEntity == null) {
            tokenEntity = new UserOAuthToken();
            tokenEntity.setUser(user);
            user.setOauthToken(tokenEntity);
        }

        tokenEntity.setAccessToken(accessToken);
        tokenEntity.setRefreshToken(refreshToken);
        tokenEntity.setTokenExpiry(expiresAt);

        // Saving the user will save the token because of CascadeType.ALL
        return userRepository.save(user);
    }

    @Transactional
    public void updateProfile(Long userId, String name, String timezone)  {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(name);
        user.setTimezone(timezone);
        userRepository.save(user);
    }



    @Override
    public User getUserByEmailPrefix(String emailPrefix) {
         User user = userRepository.findByExactEmailPrefix(emailPrefix)
                .orElseThrow(() -> new RuntimeException("User not found with email prefix: " + emailPrefix));
        return user;
    }
    
    // Admin panel methods
    @Override
    public List<User> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }
    
    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    @Override
    public Page<User> findByStatusPaginated(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable);
    }
    
    @Override
    @Transactional
    public void approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        if (user.getStatus() != UserStatus.PENDING) {
            throw new RuntimeException("User is not pending approval");
        }
        
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void rejectUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        if (user.getStatus() != UserStatus.PENDING) {
            throw new RuntimeException("User is not pending approval");
        }
        
        // For now, we'll just delete the user. 
        // In the future, you might want to store rejected users with a REJECTED status
        userRepository.delete(user);
    }
}