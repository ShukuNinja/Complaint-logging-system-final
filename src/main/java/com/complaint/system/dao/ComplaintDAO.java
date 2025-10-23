package com.complaint.system.dao;

import com.complaint.system.entity.Complaint;
import com.complaint.system.entity.Department;
import com.complaint.system.entity.User;
import org.hibernate.query.Query;

import java.util.List;

public class ComplaintDAO extends BaseDAO<Complaint> {
    @Override
    public Complaint save(Complaint complaint) {
        return executeInTransaction(session -> {
            session.persist(complaint);
            return complaint;
        });
    }

    @Override
    public Complaint update(Complaint complaint) {
        return executeInTransaction(session -> {
            session.merge(complaint);
            return complaint;
        });
    }

    @Override
    public Complaint findById(Long complaintId) {
        return executeInTransaction(session -> session.get(Complaint.class, complaintId));
    }

    @Override
    public List<Complaint> findAll() {
        return executeInTransaction(session -> {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint c LEFT JOIN FETCH c.lodgedBy LEFT JOIN FETCH c.assignedToDept ORDER BY c.lodgedAt DESC",
                Complaint.class);
            return query.getResultList();
        });
    }

    public List<Complaint> findAllPaginated(int page, int pageSize) {
        return executeInTransaction(session -> {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint c LEFT JOIN FETCH c.lodgedBy LEFT JOIN FETCH c.assignedToDept ORDER BY c.lodgedAt DESC",
                Complaint.class);
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        });
    }

    @Override
    public void delete(Complaint complaint) {
        executeInTransactionWithoutResult(session -> {
            session.remove(session.merge(complaint));
            return null;
        });
    }

    @Override
    public void deleteById(Long complaintId) {
        executeInTransactionWithoutResult(session -> {
            Complaint complaint = session.get(Complaint.class, complaintId);
            if (complaint != null) {
                session.remove(complaint);
            }
            return null;
        });
    }

    public List<Complaint> findByLodgedBy(User user) {
        return executeInTransaction(session -> {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint c LEFT JOIN FETCH c.lodgedBy LEFT JOIN FETCH c.assignedToDept " +
                "WHERE c.lodgedBy = :user ORDER BY c.lodgedAt DESC",
                Complaint.class);
            query.setParameter("user", user);
            return query.getResultList();
        });
    }

    public List<Complaint> findByDepartment(Department department) {
        return executeInTransaction(session -> {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint c LEFT JOIN FETCH c.lodgedBy LEFT JOIN FETCH c.assignedToDept " +
                "WHERE c.assignedToDept = :department ORDER BY c.lodgedAt DESC",
                Complaint.class);
            query.setParameter("department", department);
            return query.getResultList();
        });
    }

    public List<Complaint> findByStatus(Complaint.ComplaintStatus status) {
        return executeInTransaction(session -> {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint c LEFT JOIN FETCH c.lodgedBy LEFT JOIN FETCH c.assignedToDept " +
                "WHERE c.status = :status ORDER BY c.lodgedAt DESC",
                Complaint.class);
            query.setParameter("status", status);
            return query.getResultList();
        });
    }

    public List<Complaint> findByDepartmentAndStatus(Department department, Complaint.ComplaintStatus status) {
        return executeInTransaction(session -> {
            Query<Complaint> query = session.createQuery(
                "FROM Complaint c LEFT JOIN FETCH c.lodgedBy LEFT JOIN FETCH c.assignedToDept " +
                "WHERE c.assignedToDept = :department AND c.status = :status ORDER BY c.lodgedAt DESC",
                Complaint.class);
            query.setParameter("department", department);
            query.setParameter("status", status);
            return query.getResultList();
        });
    }

    public long getComplaintCount() {
        return executeInTransaction(session -> {
            Query<Long> query = session.createQuery("SELECT COUNT(c) FROM Complaint c", Long.class);
            return query.getSingleResult();
        });
    }

    public long getComplaintCountByStatus(Complaint.ComplaintStatus status) {
        return executeInTransaction(session -> {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(c) FROM Complaint c WHERE c.status = :status", Long.class);
            query.setParameter("status", status);
            return query.getSingleResult();
        });
    }
}