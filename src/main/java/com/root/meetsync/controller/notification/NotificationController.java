package com.root.meetsync.controller.notification;

import com.root.meetsync.dto.Notification.NotificationDTO;
import com.root.meetsync.entity.Notification;
import com.root.meetsync.entity.User;
import com.root.meetsync.service.NotificationService;
import com.root.meetsync.service.UserService;
import com.root.meetsync.service.booking.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final UserService userService;
    private final IBookingService bookingService;
    
    @GetMapping
    public String notificationsPage(Authentication authentication, Model model,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size,
                                   @RequestParam(required = false) String filter) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        User user = getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDTO> notifications = notificationService.getAllNotifications(user, pageable);
        Long unreadCount = notificationService.getUnreadCount(user);
        Long eventCount = notificationService.getEventNotificationCount(user);
        Long bookingCount = notificationService.getBookingNotificationCount(user);
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("eventCount", eventCount);
        model.addAttribute("bookingCount", bookingCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notifications.getTotalPages());
        model.addAttribute("user", user);
        model.addAttribute("filter", filter != null ? filter : "all");
        
        return "notifications";
    }
    
    @PostMapping("/{id}/mark-read")
    public String markAsRead(@PathVariable Long id, 
                            @RequestParam(defaultValue = "0") int page,
                            RedirectAttributes redirectAttributes) {
        notificationService.markAsRead(id);
        redirectAttributes.addFlashAttribute("message", "Notification marked as read");
        return "redirect:/notifications?page=" + page;
    }
    
    @PostMapping("/mark-all-read")
    public String markAllAsRead(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        User user = getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        
        int count = notificationService.markAllAsRead(user);
        redirectAttributes.addFlashAttribute("message", count + " notifications marked as read");
        return "redirect:/notifications";
    }
    
    @PostMapping("/{id}/delete")
    public String deleteNotification(@PathVariable Long id,
                                    @RequestParam(defaultValue = "0") int page,
                                    RedirectAttributes redirectAttributes) {
        notificationService.deleteNotification(id);
        redirectAttributes.addFlashAttribute("message", "Notification deleted");
        return "redirect:/notifications?page=" + page;
    }
    
    @PostMapping("/delete-all")
    public String deleteAllNotifications(Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }
        
        User user = getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        
        notificationService.deleteAllNotifications(user);
        redirectAttributes.addFlashAttribute("message", "All notifications deleted");
        return "redirect:/notifications";
    }
    
    
    private User getCurrentUser(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            return userService.processOAuthUser((OAuth2AuthenticationToken) authentication);
        } else {
            String email = authentication.getName();
            return userService.findByEmail(email).orElse(null);
        }
    }
}
