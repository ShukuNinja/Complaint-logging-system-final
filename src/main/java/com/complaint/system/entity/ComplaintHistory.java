package com.complaint.system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "COMPLAINT_HISTORY")
public class ComplaintHistory {
    @Id
    @Column(name = "HISTORY_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_seq")
    @SequenceGenerator(name = "history_seq", sequenceName = "history_seq", allocationSize = 1)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPLAINT_ID", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHANGED_BY", nullable = false)
    private User changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "PREVIOUS_STATUS", length = 20)
    private Complaint.ComplaintStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "NEW_STATUS", nullable = false, length = 20)
    private Complaint.ComplaintStatus newStatus;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @Column(name = "CHANGED_AT")
    private LocalDateTime changedAt;

    public ComplaintHistory() {
        this.changedAt = LocalDateTime.now();
    }

    public ComplaintHistory(Complaint complaint, User changedBy, Complaint.ComplaintStatus previousStatus,
                           Complaint.ComplaintStatus newStatus, String remarks) {
        this();
        this.complaint = complaint;
        this.changedBy = changedBy;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.remarks = remarks;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public Complaint.ComplaintStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(Complaint.ComplaintStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public Complaint.ComplaintStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(Complaint.ComplaintStatus newStatus) {
        this.newStatus = newStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public String toString() {
        return "ComplaintHistory{historyId=" + historyId + ", complaintId=" + 
               (complaint != null ? complaint.getComplaintId() : "null") + ", newStatus=" + newStatus + "}";
    }
}