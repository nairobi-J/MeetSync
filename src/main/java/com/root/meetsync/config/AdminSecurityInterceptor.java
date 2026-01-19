package com.root.meetsync.config;

import com.root.meetsync.entity.User;
import com.root.meetsync.entity.UserRole;
import com.root.meetsync.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminSecurityInterceptor implements HandlerInterceptor {
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            response.sendRedirect("/login");
            return false;
        }
        
        String email = getEmailFromAuth(auth);
        if (email != null) {
            User user = userService.findByEmail(email).orElse(null);
            if (user == null || user.getRole() != UserRole.ADMIN) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
                return false;
            }
        }
        
        return true;
    }
    
    private String getEmailFromAuth(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            return oauthToken.getPrincipal().getAttribute("email");
        }
        return auth.getName();
    }
}
