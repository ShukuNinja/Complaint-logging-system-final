package com.complaint.system.util;

import java.util.regex.Pattern;

public class PasswordValidator {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,128}$");

    public static PasswordValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordValidationResult(false, "Password is required.");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return new PasswordValidationResult(false,
                "Password must be 8-128 characters with uppercase, lowercase, digit, and special character (@$!%*?&).");
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