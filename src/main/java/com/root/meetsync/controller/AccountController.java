package com.root.meetsync.controller;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.entity.DeletionReason;
import com.root.meetsync.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Show account deletion confirmation page
     */
    @GetMapping("/delete")
    public String showDeleteAccountPage(HttpSession session, Model model) {
        CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUserDTO");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("deletionReasons", DeletionReason.values());
        return "account/delete-account";
    }

    /**
     * Process account deletion (self-deletion)
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteAccount(
            @RequestParam("reason") DeletionReason reason,
            @RequestParam("notes") String notes,
            @RequestParam("confirmEmail") String confirmEmail,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUserDTO");
            if (currentUser == null) {
                response.put("success", false);
                response.put("error", "User not authenticated");
                return ResponseEntity.badRequest().body(response);
            }

            // Verify email confirmation
            if (!currentUser.getEmail().equals(confirmEmail)) {
                response.put("success", false);
                response.put("error", "Email confirmation does not match your account email");
                return ResponseEntity.badRequest().body(response);
            }

            // Perform deletion
            userService.deleteOwnAccount(currentUser.getId(), reason, notes);
            
            // Invalidate session immediately
            session.invalidate();
            
            response.put("success", true);
            response.put("message", "Your account has been successfully deleted. You will be redirected to the login page.");
            response.put("redirectUrl", "/login?deleted=true");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete account: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
