package com.complaint.system.util;

import java.util.regex.Pattern;

public class PasswordValidator {
    // More lenient pattern - allows common special characters
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,128}$");

    public static PasswordValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordValidationResult(false, "Password is required.");
        }
        
        if (password.length() < 8) {
            return new PasswordValidationResult(false, "Password must be at least 8 characters long.");
        }
        
        if (password.length() > 128) {
            return new PasswordValidationResult(false, "Password must not exceed 128 characters.");
        }
        
        if (!password.matches(".*[a-z].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one lowercase letter.");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one uppercase letter.");
        }
        
        if (!password.matches(".*\\d.*")) {
            return new PasswordValidationResult(false, "Password must contain at least one digit.");
        }
        
        if (!password.matches(".*[^A-Za-z0-9].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one special character.");
        }
        
        return new PasswordValidationResult(true, "Password is valid.");
    }

    public static boolean isValid(String password) {
        return validate(password).isValid();
    }

    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;

        public PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}