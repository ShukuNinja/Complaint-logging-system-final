package com.complaint.system.controller;
import javafx.beans.property.SimpleStringProperty;
import com.complaint.system.entity.ComplaintHistory;
import com.complaint.system.entity.User;


import com.complaint.system.dao.ComplaintDAO;
import com.complaint.system.dao.DepartmentDAO;
import com.complaint.system.entity.Complaint;
import com.complaint.system.entity.Department;
import com.complaint.system.util.InputSanitizer;
import com.complaint.system.util.SceneManager;
import com.complaint.system.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CitizenDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private TextField titleField;
    @FXML private ComboBox<Department> departmentComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Button submitButton;
    @FXML private Label statusLabel;
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, Long> idColumn;
    @FXML private TableColumn<Complaint, String> titleColumn;
    @FXML private TableColumn<Complaint, String> departmentColumn;
    @FXML private TableColumn<Complaint, String> statusColumn;
    @FXML private TableColumn<Complaint, String> lodgedAtColumn;
    @FXML private Button viewDetailsButton;
    @FXML private Button refreshButton;

    private ComplaintDAO complaintDAO = new ComplaintDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUser().getFullName());
        setupTableColumns();
        loadDepartments();
        refreshComplaints();
        viewDetailsButton.setDisable(true);
        complaintsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, newValue) -> viewDetailsButton.setDisable(newValue == null)
        );
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("complaintId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        departmentColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getAssignedToDept().getDeptName()));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        lodgedAtColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getLodgedAt().format(dateFormatter)));
    }

    private void loadDepartments() {
        List<Department> departments = departmentDAO.findAll();
        departmentComboBox.setItems(FXCollections.observableArrayList(departments));
    }

    private void refreshComplaints() {
        List<Complaint> complaints = complaintDAO.findByLodgedBy(SessionManager.getCurrentUser());
        complaintsTable.setItems(FXCollections.observableArrayList(complaints));
    }

    @FXML
    private void handleSubmitComplaint() {
        String title = InputSanitizer.sanitizeText(titleField.getText());
        String description = InputSanitizer.sanitizeText(descriptionArea.getText());
        Department department = departmentComboBox.getValue();

        if (title.isEmpty() || description.isEmpty() || department == null) {
            showStatus("All fields are required.", false);
            return;
        }

        Complaint complaint = new Complaint(title, description, SessionManager.getCurrentUser(), department);
        complaintDAO.save(complaint);
        showStatus("Complaint submitted successfully.", true);
        clearForm();
        refreshComplaints();
    }

    @FXML
    private void handleViewDetails() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SessionManager.setSelectedComplaint(selected);
            SceneManager.loadScene("ComplaintDetailsView.fxml", "Complaint Details");
        }
    }

    @FXML
    private void handleRefresh() {
        refreshComplaints();
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        SceneManager.loadScene("LoginView.fxml", "Login");
    }

    private void showStatus(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (success ? "#28a745" : "#dc3545"));
        statusLabel.setVisible(true);
    }

    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
        departmentComboBox.getSelectionModel().clearSelection();
    }
}