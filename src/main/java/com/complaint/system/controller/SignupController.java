package com.complaint.system.controller;

import com.complaint.system.dao.UserDAO;
import com.complaint.system.entity.User;
import com.complaint.system.util.EmailService;
import com.complaint.system.util.InputSanitizer;
import com.complaint.system.util.OtpService;
import com.complaint.system.util.PasswordValidator;
import com.complaint.system.util.PendingSignup;
import com.complaint.system.util.SceneManager;
import com.complaint.system.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

public class SignupController {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<User.UserRole> roleComboBox;
    @FXML private Button signupButton;
    @FXML private Button loginLinkButton;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(User.UserRole.values()));

        // Submit on Enter from any text field.
        fullNameField.setOnAction(e -> handleSignup());
        usernameField.setOnAction(e -> handleSignup());
        emailField.setOnAction(e -> handleSignup());
        passwordField.setOnAction(e -> handleSignup());
        confirmPasswordField.setOnAction(e -> handleSignup());
    }

    @FXML
    private void handleSignup() {
        String fullName = InputSanitizer.sanitizeText(fullNameField.getText());
        String username = InputSanitizer.sanitizeText(usernameField.getText());
        String email = InputSanitizer.sanitizeText(emailField.getText());
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        User.UserRole role = roleComboBox.getValue();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showError("All fields are required.");
            return;
        }

        if (!InputSanitizer.isValidUsername(username)) {
            showError("Invalid username. Use 3-50 characters, alphanumeric and underscore only.");
            return;
        }

        if (!InputSanitizer.isValidEmail(email)) {
            showError("Invalid email format.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        PasswordValidator.PasswordValidationResult passwordResult = PasswordValidator.validate(password);
        if (!passwordResult.isValid()) {
            showError(passwordResult.getMessage());
            return;
        }

        if (userDAO.findByUsername(username).isPresent()) {
            showError("Username already taken.");
            return;
        }

        // "Create an account only when that email id is available"
        if (userDAO.findByEmail(email).isPresent()) {
            showError("An account with this email already exists.");
            return;
        }

        // Everything is valid — hold the signup in memory and email an OTP.
        // The account is only written to the DB once the code is verified.
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        PendingSignup pending = new PendingSignup(fullName, username, email, passwordHash, role);
        String code = OtpService.generateCode();
        pending.issueCode(code, OtpService.EXPIRY_MINUTES);

        sendCodeAndContinue(pending, code);
    }

    private void sendCodeAndContinue(PendingSignup pending, String code) {
        signupButton.setDisable(true);
        showSuccess("Sending verification code to " + pending.getEmail() + "…");

        new Thread(() -> {
            try {
                EmailService.sendVerificationCode(pending.getEmail(), pending.getFullName(), code);
                Platform.runLater(() -> {
                    SessionManager.setPendingSignup(pending);
                    signupButton.setDisable(false);
                    SceneManager.loadScene("VerificationView.fxml", "Verify Email");
                });
            } catch (EmailService.EmailException e) {
                Platform.runLater(() -> {
                    signupButton.setDisable(false);
                    showError(e.getMessage());
                });
            }
        }, "otp-email-sender").start();
    }

    @FXML
    private void handleLoginLink() {
        SceneManager.loadScene("LoginView.fxml", "Login");
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

    private void clearForm() {
        fullNameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }
}