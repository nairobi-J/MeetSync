package com.root.meetsync.service.impl;

import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserOAuthToken;
import com.root.meetsync.repository.UserRepository;
import com.root.meetsync.service.UserService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public UserServiceImpl(UserRepository userRepository, OAuth2AuthorizedClientService authorizedClientService) {
        this.userRepository = userRepository;
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    @Transactional
    public User processOAuthUser(OAuth2AuthenticationToken authentication) {
        // 1. Get User Profile info
        Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 2. Get the OAuth Tokens (Access & Refresh)
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

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
}