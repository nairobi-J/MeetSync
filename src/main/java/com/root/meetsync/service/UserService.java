package com.root.meetsync.service;

import com.root.meetsync.dto.UserRegistrationDto;
import com.root.meetsync.entity.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import java.util.Optional; // Add this import

public interface UserService {
    User processOAuthUser(OAuth2AuthenticationToken authentication);
    User registerNewUser(UserRegistrationDto registrationDto);

    // Add these two methods
    Optional<User> findByEmail(String email);
    void updatePassword(String email, String rawPassword);
    User getUserByEmailPrefix(String prefix);
}