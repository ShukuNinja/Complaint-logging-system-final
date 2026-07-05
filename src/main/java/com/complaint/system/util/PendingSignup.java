package com.complaint.system.util;

import com.complaint.system.entity.User;

import java.time.LocalDateTime;

/**
 * Holds a validated-but-not-yet-persisted signup while the user verifies
 * their email. The User row is only written to the DB once the OTP is
 * confirmed, so no unverified accounts ever exist.
 */
public class PendingSignup {
    private final String fullName;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final User.UserRole role;

    private String code;
    private LocalDateTime expiresAt;
    private int attemptsRemaining = 5;

    public PendingSignup(String fullName, String username, String email,
                         String passwordHash, User.UserRole role) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    /** Assigns a fresh code and resets the expiry window and attempt counter. */
    public void issueCode(String code, long validForMinutes) {
        this.code = code;
        this.expiresAt = LocalDateTime.now().plusMinutes(validForMinutes);
        this.attemptsRemaining = 5;
    }

    public boolean isExpired() {
        return expiresAt == null || LocalDateTime.now().isAfter(expiresAt);
    }

    /** Checks a submitted code, consuming one attempt on a mismatch. */
    public boolean matches(String submitted) {
        if (submitted == null) {
            return false;
        }
        boolean ok = code != null && code.equals(submitted.trim());
        if (!ok) {
            attemptsRemaining--;
        }
        return ok;
    }

    /** Builds the persistable User once verification succeeds. */
    public User toUser() {
        return new User(fullName, username, passwordHash, role, email);
    }

    public String getFullName()       { return fullName; }
    public String getUsername()       { return username; }
    public String getEmail()          { return email; }
    public User.UserRole getRole()    { return role; }
    public int getAttemptsRemaining() { return attemptsRemaining; }
}
