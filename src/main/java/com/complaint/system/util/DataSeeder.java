package com.complaint.system.util;

import com.complaint.system.dao.DepartmentDAO;
import com.complaint.system.dao.UserDAO;
import com.complaint.system.entity.Department;
import com.complaint.system.entity.User;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class DataSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private static final int BCRYPT_ROUNDS = 10;

    private DataSeeder() {}

    public static void seed() {
        try {
            DepartmentDAO departmentDAO = new DepartmentDAO();
            UserDAO userDAO = new UserDAO();

            ensureDepartment(departmentDAO, "Public Works");
            ensureUser(userDAO, "Demo Citizen", "citizen", "Password@123", User.UserRole.CITIZEN, "citizen@example.com");
            ensureUser(userDAO, "Demo Official", "official", "Password@123", User.UserRole.OFFICIAL, "official@example.com");

            logger.info("Data seeding completed.");
        } catch (Exception e) {
            logger.error("Data seeding failed", e);
        }
    }

    private static Department ensureDepartment(DepartmentDAO departmentDAO, String name) {
        Optional<Department> existing = departmentDAO.findByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        Department dept = new Department(name);
        return departmentDAO.save(dept);
    }

    private static void ensureUser(UserDAO userDAO, String fullName, String username, String rawPassword,
                                  User.UserRole role, String email) {
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
        Optional<User> existing = userDAO.findByUsername(username);

        if (existing.isPresent()) {
            User user = existing.get();
            user.setFullName(fullName);
            user.setPasswordHash(hash);
            user.setRole(role);
            user.setEmail(email);
            userDAO.update(user);
            logger.info("Updated user: {}", username);
        } else {
            User user = new User(fullName, username, hash, role, email);
            userDAO.save(user);
            logger.info("Created user: {}", username);
        }
    }
}