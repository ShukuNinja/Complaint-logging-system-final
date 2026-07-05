package com.complaint.system.controller;

import com.complaint.system.dao.UserDAO;
import com.complaint.system.entity.User;
import com.complaint.system.util.InputSanitizer;
import com.complaint.system.util.SceneManager;
import com.complaint.system.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ProfileController {
    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private Label statusLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            SceneManager.loadScene("LoginView.fxml", "Login");
            return;
        }
        fullNameField.setText(user.getFullName());
        usernameField.setText(user.getUsername());
        emailLabel.setText(user.getEmail());
        roleLabel.setText(user.getRole().toString());

        // Submit on Enter from either editable field.
        fullNameField.setOnAction(e -> handleSave());
        usernameField.setOnAction(e -> handleSave());
    }

    @FXML
    private void handleSave() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            SceneManager.loadScene("LoginView.fxml", "Login");
            return;
        }

        String fullName = InputSanitizer.sanitizeText(fullNameField.getText());
        String username = InputSanitizer.sanitizeText(usernameField.getText());

        if (fullName.isEmpty() || username.isEmpty()) {
            showStatus("Full name and username are required.", false);
            return;
        }

        if (!InputSanitizer.isValidUsername(username)) {
            showStatus("Invalid username. Use 3-50 characters, alphanumeric and underscore only.", false);
            return;
        }

        // Username must be unique (allow the user's own current username).
        Optional<User> existing = userDAO.findByUsername(username);
        if (existing.isPresent() && !existing.get().getUserId().equals(user.getUserId())) {
            showStatus("Username already taken.", false);
            return;
        }

        if (fullName.equals(user.getFullName()) && username.equals(user.getUsername())) {
            showStatus("No changes to save.", false);
            return;
        }

        try {
            user.setFullName(fullName);
            user.setUsername(username);
            User updated = userDAO.update(user);
            SessionManager.setCurrentUser(updated);
            logger.info("Profile updated for user id {}", updated.getUserId());
            showStatus("Profile updated successfully.", true);
        } catch (Exception e) {
            logger.error("Failed to update profile for user id {}", user.getUserId(), e);
            showStatus("Could not save changes. Please try again.", false);
        }
    }

    @FXML
    private void handleBack() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            SceneManager.loadScene("LoginView.fxml", "Login");
            return;
        }
        String fxml = user.getRole() == User.UserRole.CITIZEN
            ? "CitizenDashboardView.fxml" : "OfficialDashboardView.fxml";
        String title = user.getRole() == User.UserRole.CITIZEN
            ? "Citizen Dashboard" : "Official Dashboard";
        SceneManager.loadScene(fxml, title);
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        statusLabel.getStyleClass().add(success ? "success-label" : "error-label");
        statusLabel.setVisible(true);
    }
}
