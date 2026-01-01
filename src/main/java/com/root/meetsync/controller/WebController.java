package com.root.meetsync.controller;

import com.root.meetsync.entity.User;
import com.root.meetsync.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Added this import
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class WebController {

    private final UserService userService; // Added service injection

    public WebController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup";
    }

    // This is the mapping to show your set-password.html file
    @GetMapping("/set-password")
    public String showSetPasswordPage() {
        return "set-password";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        String email;

        // Check if the user logged in via Google or Manual Email/Pass
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            email = (String) oauthToken.getPrincipal().getAttributes().get("email");
        } else {
            // For manual login, the 'name' is the email/username
            email = authentication.getName();
        }

        // Use the injected service to find the user
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // If the user has no password (just signed up via Google), force them to set one
            if (user.getPassword() == null) {
                return "redirect:/set-password";
            }

            model.addAttribute("userName", user.getName());
        }

        return "dashboard"; // Returns dashboard.html
    }
}