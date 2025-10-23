package com.complaint.system.util;

import java.util.regex.Pattern;

public class InputSanitizer {
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile("[<>\"'&]");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript|vbscript)");

    public static String sanitizeText(String input) {
        if (input == null) {
            return null;
        }
        return input.trim()
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
            .replace("&", "&amp;");
    }

    public static boolean isSafe(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }
        return !DANGEROUS_PATTERN.matcher(input).find() && !SQL_INJECTION_PATTERN.matcher(input).find();
    }

    public static ValidationResult validateAndSanitize(String input) {
        if (input == null) {
            return new ValidationResult(true, null, "Input is null");
        }
        if (!isSafe(input)) {
            return new ValidationResult(false, null, "Input contains dangerous content");
        }
        String sanitized = sanitizeText(input);
        return new ValidationResult(true, sanitized, "Input is valid");
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
        return emailPattern.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
        return usernamePattern.matcher(username).matches();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String sanitizedInput;
        private final String message;

        public ValidationResult(boolean valid, String sanitizedInput, String message) {
            this.valid = valid;
            this.sanitizedInput = sanitizedInput;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getSanitizedInput() {
            return sanitizedInput;
        }

        public String getMessage() {
            return message;
        }
    }
}