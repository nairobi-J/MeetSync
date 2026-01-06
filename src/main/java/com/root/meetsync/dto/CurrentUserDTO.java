package com.root.meetsync.dto;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CurrentUserDTO {

    private String name;
    private String email;

    public CurrentUserDTO(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public static CurrentUserDTO fromPrincipal(Object principal) {

        // OAuth2 Login (Google, GitHub, etc.)
        if (principal instanceof OAuth2User oauth2User) {
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            return new CurrentUserDTO(name, email);
        }

        // Form Login
        if (principal instanceof UserDetails userDetails) {
            return new CurrentUserDTO(
                    userDetails.getUsername(),
                    userDetails.getUsername() // or fetch email from DB
            );
        }

        return null;
    }

    // getters
    public String getName() { return name; }
    public String getEmail() { return email; }
}
