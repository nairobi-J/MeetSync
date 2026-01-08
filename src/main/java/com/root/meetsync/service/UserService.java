package com.root.meetsync.service;

import com.root.meetsync.dto.UserRegistrationDto;
import com.root.meetsync.entity.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional; // Add this import

public interface UserService {
    User processOAuthUser(OAuth2AuthenticationToken authentication);
    User registerNewUser(UserRegistrationDto registrationDto);
    void updateProfile(Long userId, String name, String timezone, String profilePic  );
    String storeProfileImage(MultipartFile file) throws IOException;


    // Add these two methods
    Optional<User> findByEmail(String email);
    void updatePassword(String email, String rawPassword);
    User getUserByEmailPrefix(String prefix);
}