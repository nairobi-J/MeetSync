package com.root.meetsync.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.root.meetsync.entity.User; // Added this import
import com.root.meetsync.service.UserService;

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

            model.addAttribute("userName", user.getName());
        return "MainHome";
    }

}