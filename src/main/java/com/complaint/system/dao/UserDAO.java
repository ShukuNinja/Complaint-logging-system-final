package com.complaint.system.dao;

import com.complaint.system.entity.User;
import com.complaint.system.util.InputSanitizer;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class UserDAO extends BaseDAO<User> {
    @Override
    public User save(User user) {
        return executeInTransaction(session -> {
            session.persist(user);
            return user;
        });
    }

    @Override
    public User update(User user) {
        return executeInTransaction(session -> {
            session.merge(user);
            return user;
        });
    }

    @Override
    public User findById(Long userId) {
        return executeInTransaction(session -> session.get(User.class, userId));
    }

    @Override
    public List<User> findAll() {
        return executeInTransaction(session -> {
            Query<User> query = session.createQuery("FROM User ORDER BY fullName", User.class);
            return query.getResultList();
        });
    }

    @Override
    public void delete(User user) {
        executeInTransactionWithoutResult(session -> {
            session.remove(session.merge(user));
            return null;
        });
    }

    @Override
    public void deleteById(Long userId) {
        executeInTransactionWithoutResult(session -> {
            User user = session.get(User.class, userId);
            if (user != null) {
                session.remove(user);
            }
            return null;
        });
    }

    public Optional<User> findByUsername(String username) {
        String sanitizedUsername = InputSanitizer.sanitizeText(username);
        return executeInTransaction(session -> {
            Query<User> query = session.createQuery(
                "FROM User WHERE username = :username", User.class);
            query.setParameter("username", sanitizedUsername);
            List<User> users = query.getResultList();
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        });
    }
}