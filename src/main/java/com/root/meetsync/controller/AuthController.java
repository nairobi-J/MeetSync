package com.root.meetsync.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.root.meetsync.dto.UserRegistrationDto;
import com.root.meetsync.service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/register")
    public String registerUser(@ModelAttribute UserRegistrationDto registrationDto) {
        userService.registerNewUser(registrationDto);
        // After successful signup, redirect to login page
        return "redirect:/login?success";
    }

    // @PostMapping("/api/users/set-password")
    // public String handleSetPassword(@RequestParam("password") String password,
    //                                 Authentication authentication) {
    //     String email;
    //     if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
    //         email = (String) oauthToken.getPrincipal().getAttributes().get("email");
    //     } else {
    //         email = authentication.getName();
    //     }


    //     userService.updatePassword(email, password);

    //     return "redirect:/dashboard?passwordSet=true";
    // }
}
