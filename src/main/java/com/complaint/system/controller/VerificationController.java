package com.complaint.system.controller;

import com.complaint.system.dao.UserDAO;
import com.complaint.system.util.EmailService;
import com.complaint.system.util.OtpService;
import com.complaint.system.util.PendingSignup;
import com.complaint.system.util.SceneManager;
import com.complaint.system.util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationController {
    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    @FXML private Label infoLabel;
    @FXML private TextField codeField;
    @FXML private Button verifyButton;
    @FXML private Button resendButton;
    @FXML private Button backButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final UserDAO userDAO = new UserDAO();
    private Timeline cooldownTimeline;
    private int cooldownRemaining;

    @FXML
    private void initialize() {
        PendingSignup pending = SessionManager.getPendingSignup();
        if (pending == null) {
            // Nothing to verify — go back to login.
            SceneManager.loadScene("LoginView.fxml", "Login");
            return;
        }
        infoLabel.setText("Enter the 6-digit code we sent to " + maskEmail(pending.getEmail())
            + ". It expires in " + OtpService.EXPIRY_MINUTES + " minutes.");

        // Restrict the code field to at most 6 digits.
        codeField.setTextFormatter(new TextFormatter<>(change ->
            change.getControlNewText().matches("\\d{0,6}") ? change : null));

        // Submit on Enter, and focus the code field once the scene is shown.
        codeField.setOnAction(e -> handleVerify());
        Platform.runLater(codeField::requestFocus);

        // A code was just sent from the signup screen — start the resend cooldown.
        startResendCooldown();
    }

    @FXML
    private void handleVerify() {
        PendingSignup pending = SessionManager.getPendingSignup();
        if (pending == null) {
            SceneManager.loadScene("LoginView.fxml", "Login");
            return;
        }

        String code = codeField.getText() == null ? "" : codeField.getText().trim();
        if (code.isEmpty()) {
            showError("Please enter the verification code.");
            return;
        }

        if (pending.isExpired()) {
            showError("This code has expired. Please request a new one.");
            return;
        }

        if (!pending.matches(code)) {
            if (pending.getAttemptsRemaining() <= 0) {
                SessionManager.clearPendingSignup();
                showError("Too many incorrect attempts. Please sign up again.");
                verifyButton.setDisable(true);
            } else {
                showError("Incorrect code. " + pending.getAttemptsRemaining() + " attempt(s) remaining.");
            }
            return;
        }

        // Code is correct — persist the account now.
        try {
            userDAO.save(pending.toUser());
            SessionManager.clearPendingSignup();
            stopCooldown();
            logger.info("Account created after email verification: {}", pending.getUsername());
            SceneManager.loadScene("LoginView.fxml", "Login");
        } catch (Exception e) {
            logger.error("Failed to persist verified account for {}", pending.getUsername(), e);
            showError("Your email was verified, but the account could not be created. Please try again.");
        }
    }

    @FXML
    private void handleResend() {
        PendingSignup pending = SessionManager.getPendingSignup();
        if (pending == null) {
            SceneManager.loadScene("LoginView.fxml", "Login");
            return;
        }

        String newCode = OtpService.generateCode();
        pending.issueCode(newCode, OtpService.EXPIRY_MINUTES);

        resendButton.setDisable(true);
        resendButton.setText("Sending…");
        verifyButton.setDisable(false);
        showSuccess("Sending a new code…");

        new Thread(() -> {
            try {
                EmailService.sendVerificationCode(pending.getEmail(), pending.getFullName(), newCode);
                Platform.runLater(() -> {
                    showSuccess("A new code has been sent to " + maskEmail(pending.getEmail()) + ".");
                    startResendCooldown();
                });
            } catch (EmailService.EmailException e) {
                Platform.runLater(() -> {
                    resendButton.setText("Resend code");
                    resendButton.setDisable(false);
                    showError(e.getMessage());
                });
            }
        }, "otp-email-resender").start();
    }

    /** Disables "Resend" for RESEND_COOLDOWN_SECONDS, showing a live countdown. */
    private void startResendCooldown() {
        stopCooldown();
        cooldownRemaining = RESEND_COOLDOWN_SECONDS;
        resendButton.setDisable(true);
        resendButton.setText("Resend in " + cooldownRemaining + "s");
        cooldownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            cooldownRemaining--;
            if (cooldownRemaining <= 0) {
                stopCooldown();
                resendButton.setDisable(false);
                resendButton.setText("Resend code");
            } else {
                resendButton.setText("Resend in " + cooldownRemaining + "s");
            }
        }));
        cooldownTimeline.setCycleCount(RESEND_COOLDOWN_SECONDS);
        cooldownTimeline.play();
    }

    private void stopCooldown() {
        if (cooldownTimeline != null) {
            cooldownTimeline.stop();
            cooldownTimeline = null;
        }
    }

    @FXML
    private void handleBackToLogin() {
        stopCooldown();
        SessionManager.clearPendingSignup();
        SceneManager.loadScene("LoginView.fxml", "Login");
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "your email";
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.substring(0, 2) + "***" + domain;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
}
