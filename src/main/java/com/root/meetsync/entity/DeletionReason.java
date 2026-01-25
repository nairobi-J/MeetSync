package com.root.meetsync.entity;

public enum DeletionReason {
    SELF_REQUESTED("User requested account deletion"),
    ADMIN_INITIATED("Admin initiated account deletion"),
    POLICY_VIOLATION("Account deleted due to policy violation"),
    INACTIVE_ACCOUNT("Account deleted due to inactivity"),
    DATA_BREACH("Account deleted due to security concerns"),
    OTHER("Other reason");
    
    private final String description;
    
    DeletionReason(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
