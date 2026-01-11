package com.root.meetsync.controller;

import com.root.meetsync.dto.CurrentUserDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.root.meetsync.entity.User; // Added this import
import com.root.meetsync.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;

@Controller
public class  WebController {

    private final UserService userService; // Added service injection

    public WebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage(Authentication authentication) {
        if(authentication!= null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupPage(Authentication authentication) {
        if(authentication!= null && authentication.isAuthenticated()) {
            return "redirect:/";
        }
        return "signup";
    }

    @GetMapping("/set-password")
    public String showSetPasswordPage() {
        return "set-password";
    }

    @GetMapping("/profile")
    public String showProfilePage(Model model) {
        model.addAttribute("timezones", ZoneId.getAvailableZoneIds());
        return "userinfo";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String name,
            @RequestParam String timezone,
            @RequestParam(required = false) MultipartFile profilePic,
            HttpSession session, // Inject Session
            Authentication authentication
    ) throws IOException {

        // 1. Get current cached user to access the ID and old photo URL
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUserDTO");
        if (currentUser == null) {
            return "redirect:/login";
        }

        String profilePicUrl = currentUser.getProfilePic();

        if (profilePic != null && !profilePic.isEmpty()) {
            profilePicUrl = userService.storeProfileImage(profilePic);
        }

        userService.updateProfile(
                currentUser.getId(),
                name,
                timezone,
                profilePicUrl
        );

        // Update session (IMPORTANT)
        currentUser.setName(name);
        currentUser.setTimezone(timezone);
        currentUser.setProfilePic(profilePicUrl);

        // Re-save the updated DTO into the session
        session.setAttribute("currentUserDTO", currentUser);

        return "redirect:/profile?success=true";
    }


    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user;

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            user = userService.processOAuthUser(oauthToken);
        } else {
            // For manual login, the user must exist.
            String email;
            email = authentication.getName();
            user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Manual user not found"));
        }


        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "redirect:/set-password";
        }
        return "MainHome";
    }

}