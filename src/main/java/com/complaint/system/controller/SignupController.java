package com.complaint.system.controller;
import javafx.beans.property.SimpleStringProperty;

import com.complaint.system.dao.UserDAO;
import com.complaint.system.entity.User;
import com.complaint.system.util.InputSanitizer;
import com.complaint.system.util.PasswordValidator;
import com.complaint.system.util.SceneManager;
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

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        User user = new User(fullName, username, passwordHash, role, email);
        userDAO.save(user);
        showSuccess("Account created successfully. Please login.");
        clearForm();
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