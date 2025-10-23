package com.complaint.system.controller;
import javafx.beans.property.SimpleStringProperty;


import com.complaint.system.dao.UserDAO;
import com.complaint.system.entity.User;
import com.complaint.system.util.InputSanitizer;
import com.complaint.system.util.SceneManager;
import com.complaint.system.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signupLinkButton;
    @FXML private Label errorLabel;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String username = InputSanitizer.sanitizeText(usernameField.getText());
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        try {
            Optional<User> userOptional = userDAO.findByUsername(username);
            if (userOptional.isEmpty() || !BCrypt.checkpw(password, userOptional.get().getPasswordHash())) {
                showError("Invalid username or password.");
                return;
            }

            User user = userOptional.get();
            SessionManager.setCurrentUser(user);
            String fxml = user.getRole() == User.UserRole.CITIZEN ? "CitizenDashboardView.fxml" : "OfficialDashboardView.fxml";
            String title = user.getRole() == User.UserRole.CITIZEN ? "Citizen Dashboard" : "Official Dashboard";
            SceneManager.loadScene(fxml, title);
            logger.info("User {} logged in.", username);
        } catch (Exception e) {
            logger.error("Login failed for username: {}", username, e);
            showError("An error occurred. Please try again.");
        }
    }

    @FXML
    private void handleSignupLink() {
        SceneManager.loadScene("SignupView.fxml", "Sign Up");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}