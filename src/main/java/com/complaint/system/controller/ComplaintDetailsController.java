package com.complaint.system.controller;

import com.complaint.system.dao.ComplaintHistoryDAO;
import com.complaint.system.entity.Complaint;
import com.complaint.system.entity.ComplaintHistory;
import com.complaint.system.entity.User;
import com.complaint.system.util.SceneManager;
import com.complaint.system.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ComplaintDetailsController {
    @FXML private Label complaintIdLabel;
    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label lodgedByLabel;
    @FXML private Label departmentLabel;
    @FXML private Label statusLabel;
    @FXML private Label lodgedAtLabel;
    @FXML private Label updatedAtLabel;
    @FXML private TableView<ComplaintHistory> historyTable;
    @FXML private TableColumn<ComplaintHistory, String> changedAtColumn;
    @FXML private TableColumn<ComplaintHistory, String> changedByColumn;
    @FXML private TableColumn<ComplaintHistory, String> previousStatusColumn;
    @FXML private TableColumn<ComplaintHistory, String> newStatusColumn;
    @FXML private TableColumn<ComplaintHistory, String> remarksColumn;
    @FXML private Label noHistoryLabel;
    @FXML private Button backButton;

    private ComplaintHistoryDAO historyDAO = new ComplaintHistoryDAO();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @FXML
    private void initialize() {
        setupTableColumns();
        loadComplaintDetails();
    }

    private void setupTableColumns() {
        // Set cell value factories programmatically (NOT in FXML)
        changedAtColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getChangedAt().format(dateFormatter)));
        
        changedByColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getChangedBy().getFullName()));
        
        previousStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPreviousStatus().toString()));
        
        newStatusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNewStatus().toString()));
        
        remarksColumn.setCellValueFactory(cellData -> {
            String remarks = cellData.getValue().getRemarks();
            return new SimpleStringProperty(remarks != null && !remarks.isEmpty() ? remarks : "—");
        });
    }

    private void loadComplaintDetails() {
        Complaint complaint = SessionManager.getSelectedComplaint();
        if (complaint == null) {
            // If no complaint is selected, go back to dashboard
            handleBack();
            return;
        }

        // Load complaint details
        complaintIdLabel.setText("#" + String.valueOf(complaint.getComplaintId()));
        titleLabel.setText(complaint.getTitle());
        descriptionLabel.setText(complaint.getDescription());
        lodgedByLabel.setText(complaint.getLodgedBy().getFullName());
        departmentLabel.setText(complaint.getAssignedToDept().getDeptName());
        
        // Style status label based on status
        String status = complaint.getStatus().toString();
        statusLabel.setText(status);
        switch (complaint.getStatus()) {
            case LODGED:
                statusLabel.setStyle("-fx-text-fill: #0d6efd; -fx-font-weight: bold;");
                break;
            case IN_PROGRESS:
                statusLabel.setStyle("-fx-text-fill: #fd7e14; -fx-font-weight: bold;");
                break;
            case RESOLVED:
                statusLabel.setStyle("-fx-text-fill: #138808; -fx-font-weight: bold;");
                break;
            case CLOSED:
                statusLabel.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold;");
                break;
        }
        
        lodgedAtLabel.setText(complaint.getLodgedAt().format(dateFormatter));
        updatedAtLabel.setText(complaint.getUpdatedAt() != null ? 
            complaint.getUpdatedAt().format(dateFormatter) : "Not updated yet");

        // Load history
        List<ComplaintHistory> history = historyDAO.findByComplaint(complaint);
        if (history != null && !history.isEmpty()) {
            historyTable.setItems(FXCollections.observableArrayList(history));
            noHistoryLabel.setVisible(false);
            historyTable.setVisible(true);
        } else {
            historyTable.setVisible(false);
            noHistoryLabel.setVisible(true);
        }
    }

    @FXML
    private void handleBack() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            SceneManager.loadScene("LoginView.fxml", "Login");
            return;
        }

        String fxml = currentUser.getRole() == User.UserRole.CITIZEN ? 
            "CitizenDashboardView.fxml" : "OfficialDashboardView.fxml";
        String title = currentUser.getRole() == User.UserRole.CITIZEN ? 
            "Citizen Dashboard" : "Official Dashboard";
        SceneManager.loadScene(fxml, title);
    }
}