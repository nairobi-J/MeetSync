package com.root.meetsync.service.impl;

import com.root.meetsync.dto.UserRegistrationDto;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserOAuthToken;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, OAuth2AuthorizedClientService authorizedClientService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authorizedClientService = authorizedClientService;
        this.passwordEncoder = passwordEncoder;
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
        User user = userRepository.findByGoogleId(googleId).orElseGet(() -> {
            User newUser = new User();
            newUser.setGoogleId(googleId);
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setTimezone("UTC");
            newUser.setProfilePic(picture);
            return newUser;
        });

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
    public String storeProfileImage(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadDir = Paths.get("uploads/profile");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(filename);
        Files.write(filePath, file.getBytes());

        return "/uploads/profile/" + filename;
    }


    @Transactional
    public void updateProfile(Long userId, String name, String timezone, String profilePic)  {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(name);
        user.setTimezone(timezone);

        if (profilePic != null && !profilePic.isEmpty()) {
            user.setProfilePic(profilePic);
        }

        userRepository.save(user);
    }



    @Override
    public User getUserByEmailPrefix(String emailPrefix) {
         User user = userRepository.findByExactEmailPrefix(emailPrefix)
                .orElseThrow(() -> new RuntimeException("User not found with email prefix: " + emailPrefix));
        return user;
    }
}