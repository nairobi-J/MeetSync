package com.root.meetsync.advice;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserControllerAdvice {

    private final UserService userService;

    public GlobalUserControllerAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public CurrentUserDTO handleCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email;
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            userService.processOAuthUser(oauthToken);
            email = oauthToken.getPrincipal().getAttribute("email");
        } else {
            email = authentication.getName();
        }

        // Fetch from DB
        return userService.findByEmail(email).map(user ->
                // Manual mapping using Lombok Builder
                CurrentUserDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .googleId(user.getGoogleId())
                        .profilePic(user.getProfilePic())
                        .build()
        ).orElse(null);
    }
}