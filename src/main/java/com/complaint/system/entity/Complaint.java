package com.complaint.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COMPLAINTS")
public class Complaint {
    @Id
    @Column(name = "COMPLAINT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "complaint_seq")
    @SequenceGenerator(name = "complaint_seq", sequenceName = "complaint_seq", allocationSize = 1)
    private Long complaintId;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ComplaintStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LODGED_BY", nullable = false)
    private User lodgedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNED_TO_DEPT", nullable = false)
    private Department assignedToDept;

    @Column(name = "LODGED_AT")
    private LocalDateTime lodgedAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("changedAt DESC")
    private List<ComplaintHistory> history = new ArrayList<>();

    public enum ComplaintStatus {
        LODGED, IN_PROGRESS, RESOLVED, CLOSED
    }

    public Complaint() {
        this.lodgedAt = LocalDateTime.now();
        this.status = ComplaintStatus.LODGED;
    }

    public Complaint(String title, String description, User lodgedBy, Department assignedToDept) {
        this();
        this.title = title;
        this.description = description;
        this.lodgedBy = lodgedBy;
        this.assignedToDept = assignedToDept;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public User getLodgedBy() {
        return lodgedBy;
    }

    public void setLodgedBy(User lodgedBy) {
        this.lodgedBy = lodgedBy;
    }

    public Department getAssignedToDept() {
        return assignedToDept;
    }

    public void setAssignedToDept(Department assignedToDept) {
        this.assignedToDept = assignedToDept;
    }

    public LocalDateTime getLodgedAt() {
        return lodgedAt;
    }

    public void setLodgedAt(LocalDateTime lodgedAt) {
        this.lodgedAt = lodgedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ComplaintHistory> getHistory() {
        return history;
    }

    public void setHistory(List<ComplaintHistory> history) {
        this.history = history;
    }

    @Override
    public String toString() {
        return "Complaint{complaintId=" + complaintId + ", title='" + title + "', status=" + status + "}";
    }
}