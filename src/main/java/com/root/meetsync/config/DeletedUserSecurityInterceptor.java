package com.root.meetsync.config;

import com.root.meetsync.dto.CurrentUserDTO;
import com.root.meetsync.entity.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to block all access for deleted users.
 * Forces logout and redirects deleted users to the login page.
 */
@Component
public class DeletedUserSecurityInterceptor implements HandlerInterceptor {

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
            
            if (currentUser != null && UserStatus.DELETED.equals(currentUser.getStatus())) {
                // Invalidate the session immediately
                session.invalidate();
                
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
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Account has been deleted\", \"redirect\": \"/login?deleted=true\"}");
                } else {
                    // Redirect for regular page requests
                    response.sendRedirect("/login?deleted=true");
                }
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isAllowedPath(String requestURI) {
        // Allow access to public paths even for deleted users
        return requestURI.equals("/") ||
               requestURI.equals("/login") ||
               requestURI.equals("/oauth2/authorization/google") ||
               requestURI.startsWith("/oauth2/") ||
               requestURI.equals("/error") ||
               requestURI.startsWith("/css/") ||
               requestURI.startsWith("/js/") ||
               requestURI.startsWith("/images/") ||
               requestURI.startsWith("/favicon") ||
               requestURI.startsWith("/static/") ||
               requestURI.startsWith("/webjars/");
    }
}
