package com.complaint.system.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Sends transactional email (OTP verification) via the Brevo HTTP API. */
public final class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private EmailService() {}

    /** Raised when an email cannot be sent, with a user-friendly message. */
    public static class EmailException extends Exception {
        public EmailException(String message, Throwable cause) {
            super(message, cause);
        }
        public EmailException(String message) {
            super(message);
        }
    }

    /**
     * Sends a verification code to the given address via Brevo.
     * @throws EmailException if email is not configured or delivery fails.
     */
    public static void sendVerificationCode(String toEmail, String fullName, String code)
            throws EmailException {
        if (EmailConfig.isSimulate()) {
            logger.warn("SIMULATE MODE — no email sent. Verification code for {} is: {}", toEmail, code);
            return;
        }
        if (!EmailConfig.isConfigured()) {
            throw new EmailException(
                "Email is not configured. Add your Brevo API key and verified sender to email.properties.");
        }

        String payload = buildPayload(toEmail, fullName, code);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BREVO_ENDPOINT))
                .timeout(Duration.ofSeconds(15))
                .header("api-key", EmailConfig.getApiKey())
                .header("content-type", "application/json")
                .header("accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status >= 200 && status < 300) {
                logger.info("Verification email sent to {} (HTTP {})", toEmail, status);
                return;
            }
            logger.error("Brevo rejected the email to {} (HTTP {}): {}", toEmail, status, response.body());
            throw new EmailException(friendlyError(status));
        } catch (java.io.IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            logger.error("Failed to reach Brevo to email {}", toEmail, e);
            throw new EmailException(
                "Could not send the verification email. Check your internet connection and try again.", e);
        }
    }

    private static String friendlyError(int status) {
        if (status == 401) {
            return "Email service rejected the API key. Check your Brevo API key in email.properties.";
        }
        if (status == 400) {
            return "Email could not be sent. Make sure your sender address is verified in Brevo.";
        }
        return "Email service error (HTTP " + status + "). Please try again later.";
    }

    private static String buildPayload(String toEmail, String fullName, String code) {
        String safeName = (fullName == null || fullName.isBlank()) ? "there" : fullName;
        String html = buildHtmlBody(safeName, code);
        return "{"
             + "\"sender\":{\"name\":\"" + esc(EmailConfig.getFromName()) + "\",\"email\":\"" + esc(EmailConfig.getFrom()) + "\"},"
             + "\"to\":[{\"email\":\"" + esc(toEmail) + "\",\"name\":\"" + esc(safeName) + "\"}],"
             + "\"subject\":\"Your verification code: " + esc(code) + "\","
             + "\"htmlContent\":\"" + esc(html) + "\""
             + "}";
    }

    private static String buildHtmlBody(String fullName, String code) {
        return "<div style=\"font-family:Segoe UI,Arial,sans-serif;max-width:480px;margin:auto;"
             + "border:1px solid #DDE3EE;border-radius:12px;overflow:hidden\">"
             + "<div style=\"background:#FB8C00;padding:18px 24px;color:#fff;font-size:18px;font-weight:bold\">"
             + "Complaint Management System</div>"
             + "<div style=\"padding:24px;color:#1F2937\">"
             + "<p>Hi " + htmlEscape(fullName) + ",</p>"
             + "<p>Use the code below to verify your email and finish creating your account:</p>"
             + "<div style=\"font-size:32px;font-weight:bold;letter-spacing:6px;color:#0B2E6B;"
             + "text-align:center;padding:16px;background:#F4F6FB;border-radius:10px;margin:16px 0\">"
             + htmlEscape(code) + "</div>"
             + "<p style=\"color:#6B7280;font-size:13px\">This code expires in "
             + OtpService.EXPIRY_MINUTES + " minutes. If you didn't request this, you can ignore this email.</p>"
             + "</div></div>";
    }

    /** Escapes a value for embedding inside a JSON string. */
    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /** Escapes a value for embedding inside HTML text. */
    private static String htmlEscape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
