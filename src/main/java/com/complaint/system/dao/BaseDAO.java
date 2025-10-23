package com.complaint.system.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

public abstract class BaseDAO<T> {
    private static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    private static volatile SessionFactory sessionFactory;

    private static SessionFactory getOrCreateSessionFactory() {
        if (sessionFactory == null) {
            synchronized (BaseDAO.class) {
                if (sessionFactory == null) {
                    try {
                        sessionFactory = new Configuration().configure().buildSessionFactory();
                    } catch (Exception e) {
                        logger.error("Error creating SessionFactory", e);
                        throw new RuntimeException("Failed to initialize Hibernate SessionFactory", e);
                    }
                }
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            synchronized (BaseDAO.class) {
                if (sessionFactory != null) {
                    sessionFactory.close();
                    sessionFactory = null;
                    logger.info("SessionFactory closed");
                }
            }
        }
    }

    protected SessionFactory getSessionFactory() {
        return getOrCreateSessionFactory();
    }

    protected <R> R executeInTransaction(Function<Session, R> operation) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = getSessionFactory().openSession();
            transaction = session.beginTransaction();
            R result = operation.apply(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error executing transaction", e);
            throw new RuntimeException("Database operation failed", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    protected void executeInTransactionWithoutResult(Function<Session, Void> operation) {
        executeInTransaction(session -> {
            operation.apply(session);
            return null;
        });
    }

    public abstract T save(T entity);
    public abstract T update(T entity);
    public abstract T findById(Long id);
    public abstract List<T> findAll();
    public abstract void delete(T entity);
    public abstract void deleteById(Long id);
}