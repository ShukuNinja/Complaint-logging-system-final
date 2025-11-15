package com.complaint.system;

import com.complaint.system.dao.BaseDAO;
import com.complaint.system.util.DataSeeder;
import com.complaint.system.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Citizen Complaint Management System");
            
            // Configure stage BEFORE setting it in SceneManager
            primaryStage.setResizable(true);
            primaryStage.setMaximized(false);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(768);
            primaryStage.setWidth(1280);
            primaryStage.setHeight(800);
            
            SceneManager.setPrimaryStage(primaryStage);
            DataSeeder.seed();
            SceneManager.loadScene("LoginView.fxml", "Citizen Complaint Management System - Login");
            
            primaryStage.show();
        } catch (Exception e) {
            logger.error("Error starting application", e);
            throw new RuntimeException("Failed to start application", e);
        }
    }

    @Override
    public void stop() {
        logger.info("Shutting down application");
        BaseDAO.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}