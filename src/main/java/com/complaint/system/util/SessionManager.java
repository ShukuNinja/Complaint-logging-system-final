package com.complaint.system.util;

import com.complaint.system.entity.Complaint;
import com.complaint.system.entity.User;

public class SessionManager {
    private static User currentUser;
    private static Complaint selectedComplaint;
    private static PendingSignup pendingSignup;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setSelectedComplaint(Complaint complaint) {
        selectedComplaint = complaint;
    }

    public static Complaint getSelectedComplaint() {
        return selectedComplaint;
    }

    public static void setPendingSignup(PendingSignup signup) {
        pendingSignup = signup;
    }

    public static PendingSignup getPendingSignup() {
        return pendingSignup;
    }

    public static void clearPendingSignup() {
        pendingSignup = null;
    }

    public static void clearSession() {
        currentUser = null;
        selectedComplaint = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean hasRole(User.UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }
}