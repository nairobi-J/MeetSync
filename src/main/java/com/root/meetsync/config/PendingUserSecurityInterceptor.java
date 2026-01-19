package com.root.meetsync.config;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.entity.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to protect all routes from pending users.
 * Redirects pending users to the pending approval page for any protected route.
 */
@Component
public class PendingUserSecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // Skip this interceptor for certain paths
        if (isAllowedPath(requestURI)) {
            return true;
        }
        
        // Check user status from session
        HttpSession session = request.getSession(false);
        if (session != null) {
            CurrentUserDTO currentUser = (CurrentUserDTO) session.getAttribute("currentUserDTO");
            
            if (currentUser != null && UserStatus.PENDING.equals(currentUser.getStatus())) {
                // Check if this is an AJAX/API request
                String contentType = request.getHeader("Content-Type");
                String accept = request.getHeader("Accept");
                String xRequestedWith = request.getHeader("X-Requested-With");
                
                boolean isAjaxRequest = "XMLHttpRequest".equals(xRequestedWith) ||
                                      (accept != null && accept.contains("application/json")) ||
                                      (contentType != null && contentType.contains("application/json")) ||
                                      requestURI.startsWith("/api/");
                
                if (isAjaxRequest) {
                    // Return JSON error for AJAX requests
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Account pending approval\", \"redirect\": \"/pending-approval\"}");
                } else {
                    // Redirect for regular page requests
                    response.sendRedirect("/pending-approval");
                }
                return false;
            }
        }
        
        return true; // Allow request to proceed
    }
    
    /**
     * Paths that pending users are allowed to access
     */
    private boolean isAllowedPath(String path) {
        return path.equals("/pending-approval") ||
               path.equals("/logout") ||
               path.startsWith("/css/") ||
               path.startsWith("/js/") ||
               path.startsWith("/images/") ||
               path.startsWith("/favicon") ||
               path.equals("/login") ||
               path.equals("/signup") ||
               path.equals("/") ||
               path.startsWith("/oauth2/") ||
               path.startsWith("/u/") ||  // Public booking pages
               path.startsWith("/event/participant/") ||  // Public participant pages for events
               path.startsWith("/api/events/participant/"); // Public API calls for event participation
    }
}
