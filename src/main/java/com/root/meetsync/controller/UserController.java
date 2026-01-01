package com.root.meetsync.controller;


import com.root.meetsync.dto.UserResponseDto;
import com.root.meetsync.entity.User;
import com.root.meetsync.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        String email;


        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            email = (String) oauthToken.getPrincipal().getAttributes().get("email");
        } else {
            //handles manual login
            email = authentication.getName();
        }


        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));


        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setTimezone(user.getTimezone());
        dto.setHasRefreshToken(user.getOauthToken() != null && user.getOauthToken().getRefreshToken() != null);

        return ResponseEntity.ok(dto);
    }
}
