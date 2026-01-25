package com.root.meetsync.controller;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.entity.DeletionReason;
import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserStatus;
import com.root.meetsync.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Get basic stats
        List<User> allUsers = userService.findAllUsers();
        List<User> pendingUsers = userService.findByStatus(UserStatus.PENDING);
        List<User> activeUsers = userService.findByStatus(UserStatus.ACTIVE);
        
        // Stats for cards
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("activeUsers", activeUsers.size());
        model.addAttribute("pendingUsers", pendingUsers.size());
        model.addAttribute("pendingCount", pendingUsers.size()); // For sidebar badge
        
        // Recent users for activity table (last 10)
        List<User> recentUsers = allUsers.stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .limit(10)
                .toList();
        model.addAttribute("recentUsers", recentUsers);
        
        return "admin/dashboard";
    }
    
    @GetMapping("/pending-users")
    public String pendingUsers(Model model,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> pendingUsers = userService.findByStatusPaginated(UserStatus.PENDING, pageable);
        
        model.addAttribute("pendingUsers", pendingUsers);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pendingUsers.getTotalPages());
        
        return "admin/pending-users";
    }
    
    @PostMapping("/users/{userId}/approve")
    public String approveUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.approveUser(userId);
            redirectAttributes.addAttribute("success", "User approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Failed to approve user: " + e.getMessage());
        }
        return "redirect:/admin/pending-users";
    }
    
    @PostMapping("/users/{userId}/reject")
    public String rejectUser(@PathVariable Long userId, 
                           @RequestParam(required = false) String reason,
                           RedirectAttributes redirectAttributes) {
        try {
            userService.rejectUser(userId, reason);
            redirectAttributes.addAttribute("success", "User rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Failed to reject user: " + e.getMessage());
        }
        return "redirect:/admin/pending-users";
    }
    
    /**
     * Show all users (for admin user management)
     */
    @GetMapping("/users")
    public String manageUsers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(defaultValue = "all") String status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users;
        
        // Filter by status
        switch (status.toLowerCase()) {
            case "active" -> users = userService.findByStatusPaginated(UserStatus.ACTIVE, pageable);
            case "pending" -> users = userService.findByStatusPaginated(UserStatus.PENDING, pageable);
            case "deleted" -> users = userService.findByStatusPaginated(UserStatus.DELETED, pageable);
            default -> users = userService.findAllUsersPaginated(pageable);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("currentStatus", status);
        model.addAttribute("deletionReasons", DeletionReason.values());
        
        return "admin/users";
    }
    
    /**
     * Admin delete user endpoint
     */
    @PostMapping("/users/{userId}/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable Long userId,
            @RequestParam("reason") DeletionReason reason,
            @RequestParam("notes") String notes,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            CurrentUserDTO currentAdmin = (CurrentUserDTO) session.getAttribute("currentUserDTO");
            if (currentAdmin == null) {
                response.put("success", false);
                response.put("error", "Admin not authenticated");
                return ResponseEntity.badRequest().body(response);
            }

            // Perform admin deletion
            userService.deleteUserByAdmin(userId, currentAdmin.getId(), reason, notes);
            
            response.put("success", true);
            response.put("message", "User account has been successfully deleted.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
