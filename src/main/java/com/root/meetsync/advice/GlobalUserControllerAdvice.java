package com.root.meetsync.advice;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserStatus;
import com.root.meetsync.service.NotificationService;
import com.root.meetsync.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserControllerAdvice {

    private final UserService userService;
    private final NotificationService notificationService;

    public GlobalUserControllerAdvice(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @ModelAttribute("currentUser")
    public CurrentUserDTO handleCurrentUser(Authentication authentication, HttpSession session) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

       
        CurrentUserDTO cachedUser = (CurrentUserDTO) session.getAttribute("currentUserDTO");
        if (cachedUser != null) {
            return cachedUser;
        }

        String email;
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            try {
                userService.processOAuthUser(oauthToken);
                email = oauthToken.getPrincipal().getAttribute("email");
            } catch (Exception e) {
                // Log error but don't fail the request
                System.err.println("Error processing OAuth user: " + e.getMessage());
                return null;
            }
        } else {
            email = authentication.getName();
        }

        // Fetch user from DB
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return null;
        }
        
        // Check if user is pending and store in session
        if (user.getStatus() == UserStatus.PENDING) {
            session.setAttribute("userStatus", "PENDING");
        }
        
        CurrentUserDTO currentUser = CurrentUserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .googleId(user.getGoogleId())
                .profilePic(user.getProfilePic())
                .timezone(user.getTimezone())
                .status(user.getStatus())
                .role(user.getRole())
                .build();

        // Store DTO in session
        session.setAttribute("currentUserDTO", currentUser);

        return currentUser;
    }
    
    @ModelAttribute("unreadNotificationCount")
    public Long getUnreadNotificationCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return 0L;
        }
        
        String email;
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            email = oauthToken.getPrincipal().getAttribute("email");
        } else {
            email = authentication.getName();
        }
        
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return 0L;
        }
        
        return notificationService.getUnreadCount(user);
    }
}