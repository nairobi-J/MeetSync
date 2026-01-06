package com.root.meetsync.advice;

import com.root.meetsync.dto.CurrentUserDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserControllerAdvice {

    @ModelAttribute("currentUser")
    public CurrentUserDTO currentUser(@AuthenticationPrincipal Object principal) {
        if (principal == null) return null;
        return CurrentUserDTO.fromPrincipal(principal);
    }
}

