package com.complaint.system.dao;

import com.complaint.system.entity.Department;
import com.complaint.system.util.InputSanitizer;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class DepartmentDAO extends BaseDAO<Department> {
    @Override
    public Department save(Department department) {
        return executeInTransaction(session -> {
            session.persist(department);
            return department;
        });
    }

    @Override
    public Department update(Department department) {
        return executeInTransaction(session -> {
            session.merge(department);
            return department;
        });
    }

    @Override
    public Department findById(Long deptId) {
        return executeInTransaction(session -> session.get(Department.class, deptId));
    }

    @Override
    public List<Department> findAll() {
        return executeInTransaction(session -> {
            Query<Department> query = session.createQuery("FROM Department ORDER BY deptName", Department.class);
            return query.getResultList();
        });
    }

    @Override
    public void delete(Department department) {
        executeInTransactionWithoutResult(session -> {
            session.remove(session.merge(department));
            return null;
        });
    }

    @Override
    public void deleteById(Long deptId) {
        executeInTransactionWithoutResult(session -> {
            Department department = session.get(Department.class, deptId);
            if (department != null) {
                session.remove(department);
            }
            return null;
        });
    }

    public Optional<Department> findByName(String deptName) {
        String sanitizedName = InputSanitizer.sanitizeText(deptName);
        return executeInTransaction(session -> {
            Query<Department> query = session.createQuery(
                "FROM Department WHERE deptName = :deptName", Department.class);
            query.setParameter("deptName", sanitizedName);
            List<Department> departments = query.getResultList();
            return departments.isEmpty() ? Optional.empty() : Optional.of(departments.get(0));
        });
    }
}