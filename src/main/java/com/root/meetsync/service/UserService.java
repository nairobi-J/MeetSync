package com.root.meetsync.service;


import com.root.meetsync.entity.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public interface UserService {
    User processOAuthUser(OAuth2AuthenticationToken authentication);
}
