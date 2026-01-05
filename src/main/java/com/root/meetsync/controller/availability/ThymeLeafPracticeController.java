//package com.root.meetsync.controller.availability;
//
//import java.util.Map;
//
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.root.meetsync.dto.UserResponseDto;
//import com.root.meetsync.entity.User;
//import com.root.meetsync.service.UserService;
//
//import org.springframework.ui.Model;
//import lombok.RequiredArgsConstructor;
//
//@RequiredArgsConstructor
//@Controller
//public class ThymeLeafPracticeController {
//
//
//
//    private final UserService userService;
//
//    @GetMapping("/user-info")
//    public String getUserInfo(Model model,OAuth2AuthenticationToken authentication) {
//
//
//
//        User user = userService.processOAuthUser(authentication);
//
//
//    Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
//
//    String profilePic = (String) attributes.get("picture");
//        // Convert Entity to userdto
//       UserResponseDto userdto = new UserResponseDto();
//
//        userdto.setId(user.getId());
//        userdto.setName(user.getName());
//        userdto.setEmail(user.getEmail());
//        userdto.setTimezone(user.getTimezone());
//        userdto.setHasRefreshToken(user.getOauthToken() != null && user.getOauthToken().getRefreshToken() != null);
//        userdto.setProfilePic(profilePic);
//
//        model.addAttribute("user", userdto);
//        model.addAttribute("uid",userdto.getId());
//
//        return "userinfo"; // Return the Thymeleaf template name
//    }
//
//
//
//
//
//}
