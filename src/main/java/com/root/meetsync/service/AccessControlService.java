package com.root.meetsync.service;

import com.root.meetsync.entity.UserRole;
import com.root.meetsync.entity.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

@Service
public class AccessControlService {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessControlService.class);
    
    @Value("${app.allowed-domains:dsinnovators.com}")
    private String allowedDomainsString;
    
    @Value("${app.admin-emails:}")
    private String adminEmailsString;
    
    public UserStatus determineUserStatus(String email) {
        logger.info("Determining status for email: {}", email);
        boolean allowedDomain = isAllowedDomain(email);
        boolean adminEmail = isAdminEmail(email);
        logger.info("Email: {} - AllowedDomain: {}, AdminEmail: {}", email, allowedDomain, adminEmail);
        
        if (allowedDomain || adminEmail) {
            logger.info("Setting status to ACTIVE for email: {}", email);
            return UserStatus.ACTIVE;
        }
        logger.info("Setting status to PENDING for email: {}", email);
        return UserStatus.PENDING;
    }
    
    public UserRole determineUserRole(String email) {
        logger.info("Determining role for email: {}", email);
        if (isAdminEmail(email)) {
            logger.info("Setting role to ADMIN for email: {}", email);
            return UserRole.ADMIN;
        }
        logger.info("Setting role to USER for email: {}", email);
        return UserRole.USER;
    }
    
    private boolean isAllowedDomain(String email) {
        if (allowedDomainsString == null || allowedDomainsString.isEmpty()) {
            return false;
        }
        List<String> domains = Arrays.asList(allowedDomainsString.split(","));
        return domains.stream()
            .anyMatch(domain -> email.endsWith("@" + domain.trim()));
    }
    
    private boolean isAdminEmail(String email) {
        if (adminEmailsString == null || adminEmailsString.isEmpty()) {
            return false;
        }
        List<String> adminEmails = Arrays.asList(adminEmailsString.split(","));
        return adminEmails.stream()
            .anyMatch(adminEmail -> email.equals(adminEmail.trim()));
    }
    
    public boolean hasAdminAccess(UserRole role) {
        return role == UserRole.ADMIN;
    }
}
