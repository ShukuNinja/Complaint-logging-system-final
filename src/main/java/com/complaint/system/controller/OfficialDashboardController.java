package com.complaint.system.controller;

import com.complaint.system.dao.ComplaintDAO;
import com.complaint.system.dao.ComplaintHistoryDAO;
import com.complaint.system.dao.DepartmentDAO;
import com.complaint.system.entity.Complaint;
import com.complaint.system.entity.ComplaintHistory;
import com.complaint.system.entity.Department;
import com.complaint.system.util.InputSanitizer;
import com.complaint.system.util.SceneManager;
import com.complaint.system.util.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OfficialDashboardController {
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;
    @FXML private ComboBox<Department> departmentFilterComboBox;
    @FXML private ComboBox<Complaint.ComplaintStatus> statusFilterComboBox;
    @FXML private Button applyFilterButton;
    @FXML private Button clearFilterButton;
    @FXML private Button refreshButton;
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, Long> idColumn;
    @FXML private TableColumn<Complaint, String> titleColumn;
    @FXML private TableColumn<Complaint, String> lodgedByColumn;
    @FXML private TableColumn<Complaint, String> departmentColumn;
    @FXML private TableColumn<Complaint, String> statusColumn;
    @FXML private TableColumn<Complaint, String> lodgedAtColumn;
    @FXML private VBox complaintDetailsPanel;
    @FXML private Label detailTitleLabel;
    @FXML private Label detailDescriptionLabel;
    @FXML private Label detailLodgedByLabel;
    @FXML private Label detailDepartmentLabel;
    @FXML private Label detailStatusLabel;
    @FXML private Label detailLodgedAtLabel;
    @FXML private ComboBox<Complaint.ComplaintStatus> statusUpdateComboBox;
    @FXML private TextArea remarksArea;
    @FXML private Button updateStatusButton;
    @FXML private Label updateStatusLabel;

    private ComplaintDAO complaintDAO = new ComplaintDAO();
    private DepartmentDAO departmentDAO = new DepartmentDAO();
    private ComplaintHistoryDAO historyDAO = new ComplaintHistoryDAO();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUser().getFullName());
        setupTableColumns();
        loadFilters();
        refreshComplaints();
        complaintDetailsPanel.setVisible(false);
        complaintsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, newValue) -> showComplaintDetails(newValue)
        );
    }

    private void setupTableColumns() {
        // Set cell value factories programmatically (NOT in FXML)
        idColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().getComplaintId()));
        
        titleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTitle()));
        
        lodgedByColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getLodgedBy().getFullName()));
        
        departmentColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getAssignedToDept().getDeptName()));
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus().toString()));
        
        lodgedAtColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getLodgedAt().format(dateFormatter)));
    }

    private void loadFilters() {
        // Department filter
        List<Department> departments = new ArrayList<>(departmentDAO.findAll());
        departmentFilterComboBox.setItems(FXCollections.observableArrayList(departments));
        departmentFilterComboBox.setPromptText("All Departments");
        
        // Status filter
        List<Complaint.ComplaintStatus> statuses = new ArrayList<>(Arrays.asList(Complaint.ComplaintStatus.values()));
        statusFilterComboBox.setItems(FXCollections.observableArrayList(statuses));
        statusFilterComboBox.setPromptText("All Statuses");
        
        // Status update combo
        statusUpdateComboBox.setItems(FXCollections.observableArrayList(Complaint.ComplaintStatus.values()));
    }

    private void refreshComplaints() {
        Department department = departmentFilterComboBox.getValue();
        Complaint.ComplaintStatus status = statusFilterComboBox.getValue();
        List<Complaint> complaints;
        
        if (department != null && status != null) {
            complaints = complaintDAO.findByDepartmentAndStatus(department, status);
        } else if (department != null) {
            complaints = complaintDAO.findByDepartment(department);
        } else if (status != null) {
            complaints = complaintDAO.findByStatus(status);
        } else {
            complaints = complaintDAO.findAllPaginated(1, 100);
        }
        
        complaintsTable.setItems(FXCollections.observableArrayList(complaints));
    }

    private void showComplaintDetails(Complaint complaint) {
        complaintDetailsPanel.setVisible(complaint != null);
        if (complaint != null) {
            detailTitleLabel.setText(complaint.getTitle());
            detailDescriptionLabel.setText(complaint.getDescription());
            detailLodgedByLabel.setText(complaint.getLodgedBy().getFullName());
            detailDepartmentLabel.setText(complaint.getAssignedToDept().getDeptName());
            detailStatusLabel.setText(complaint.getStatus().toString());
            detailLodgedAtLabel.setText(complaint.getLodgedAt().format(dateFormatter));
            statusUpdateComboBox.getSelectionModel().select(complaint.getStatus());
            remarksArea.clear();
            updateStatusLabel.setVisible(false);
        }
    }

    @FXML
    private void handleApplyFilter() {
        refreshComplaints();
    }

    @FXML
    private void handleClearFilter() {
        departmentFilterComboBox.getSelectionModel().clearSelection();
        statusFilterComboBox.getSelectionModel().clearSelection();
        refreshComplaints();
    }

    @FXML
    private void handleRefresh() {
        refreshComplaints();
    }

    @FXML
    private void handleUpdateStatus() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showUpdateStatus("Please select a complaint.", false);
            return;
        }

        Complaint.ComplaintStatus newStatus = statusUpdateComboBox.getValue();
        String remarks = InputSanitizer.sanitizeText(remarksArea.getText());
        
        if (newStatus == null) {
            showUpdateStatus("Please select a new status.", false);
            return;
        }

        if (newStatus == selected.getStatus() && remarks.isEmpty()) {
            showUpdateStatus("No changes detected.", false);
            return;
        }

        Complaint.ComplaintStatus previousStatus = selected.getStatus();
        selected.setStatus(newStatus);
        selected.setUpdatedAt(java.time.LocalDateTime.now());
        complaintDAO.update(selected);

        ComplaintHistory history = new ComplaintHistory(
            selected, SessionManager.getCurrentUser(), previousStatus, newStatus, remarks);
        historyDAO.save(history);

        showUpdateStatus("Status updated successfully.", true);
        refreshComplaints();
        showComplaintDetails(selected);
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        SceneManager.loadScene("LoginView.fxml", "Login");
    }

    private void showUpdateStatus(String message, boolean success) {
        updateStatusLabel.setText(message);
        updateStatusLabel.setStyle("-fx-text-fill: " + (success ? "#28a745" : "#dc3545"));
        updateStatusLabel.setVisible(true);
    }
}