package com.complaint.system.dao;

import com.complaint.system.entity.Complaint;
import com.complaint.system.entity.ComplaintHistory;
import org.hibernate.query.Query;

import java.util.List;

public class ComplaintHistoryDAO extends BaseDAO<ComplaintHistory> {
    @Override
    public ComplaintHistory save(ComplaintHistory complaintHistory) {
        return executeInTransaction(session -> {
            session.persist(complaintHistory);
            return complaintHistory;
        });
    }

    @Override
    public ComplaintHistory update(ComplaintHistory complaintHistory) {
        return executeInTransaction(session -> {
            session.merge(complaintHistory);
            return complaintHistory;
        });
    }

    @Override
    public ComplaintHistory findById(Long historyId) {
        return executeInTransaction(session -> session.get(ComplaintHistory.class, historyId));
    }

    @Override
    public List<ComplaintHistory> findAll() {
        return executeInTransaction(session -> {
            Query<ComplaintHistory> query = session.createQuery(
                "FROM ComplaintHistory ch LEFT JOIN FETCH ch.complaint LEFT JOIN FETCH ch.changedBy " +
                "ORDER BY ch.changedAt DESC",
                ComplaintHistory.class);
            return query.getResultList();
        });
    }

    @Override
    public void delete(ComplaintHistory complaintHistory) {
        executeInTransactionWithoutResult(session -> {
            session.remove(session.merge(complaintHistory));
            return null;
        });
    }

    @Override
    public void deleteById(Long historyId) {
        executeInTransactionWithoutResult(session -> {
            ComplaintHistory complaintHistory = session.get(ComplaintHistory.class, historyId);
            if (complaintHistory != null) {
                session.remove(complaintHistory);
            }
            return null;
        });
    }

    public List<ComplaintHistory> findByComplaint(Complaint complaint) {
        return executeInTransaction(session -> {
            Query<ComplaintHistory> query = session.createQuery(
                "FROM ComplaintHistory ch LEFT JOIN FETCH ch.complaint LEFT JOIN FETCH ch.changedBy " +
                "WHERE ch.complaint = :complaint ORDER BY ch.changedAt DESC",
                ComplaintHistory.class);
            query.setParameter("complaint", complaint);
            return query.getResultList();
        });
    }
}