package com.complaint.system.controller;
import javafx.beans.property.SimpleStringProperty;
import com.complaint.system.entity.User;
import com.complaint.system.entity.ComplaintHistory;



import com.complaint.system.dao.ComplaintHistoryDAO;
import com.complaint.system.entity.Complaint;
import com.complaint.system.entity.ComplaintHistory;
import com.complaint.system.util.SceneManager;
import com.complaint.system.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
        changedAtColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getChangedAt().format(dateFormatter)));
        changedByColumn.setCellValueFactory(cell -> 
            new SimpleStringProperty(cell.getValue().getChangedBy().getFullName()));
        previousStatusColumn.setCellValueFactory(new PropertyValueFactory<>("previousStatus"));
        newStatusColumn.setCellValueFactory(new PropertyValueFactory<>("newStatus"));
        remarksColumn.setCellValueFactory(new PropertyValueFactory<>("remarks"));
    }

    private void loadComplaintDetails() {
        Complaint complaint = SessionManager.getSelectedComplaint();
        if (complaint != null) {
            complaintIdLabel.setText(String.valueOf(complaint.getComplaintId()));
            titleLabel.setText(complaint.getTitle());
            descriptionLabel.setText(complaint.getDescription());
            lodgedByLabel.setText(complaint.getLodgedBy().getFullName());
            departmentLabel.setText(complaint.getAssignedToDept().getDeptName());
            statusLabel.setText(complaint.getStatus().toString());
            lodgedAtLabel.setText(complaint.getLodgedAt().format(dateFormatter));
            updatedAtLabel.setText(complaint.getUpdatedAt() != null ? 
                complaint.getUpdatedAt().format(dateFormatter) : "N/A");

            List<ComplaintHistory> history = historyDAO.findByComplaint(complaint);
            historyTable.setItems(FXCollections.observableArrayList(history));
            noHistoryLabel.setVisible(history.isEmpty());
        }
    }

    @FXML
    private void handleBack() {
        String fxml = SessionManager.hasRole(User.UserRole.CITIZEN) ? 
            "CitizenDashboardView.fxml" : "OfficialDashboardView.fxml";
        String title = SessionManager.hasRole(User.UserRole.CITIZEN) ? 
            "Citizen Dashboard" : "Official Dashboard";
        SceneManager.loadScene(fxml, title);
    }
}