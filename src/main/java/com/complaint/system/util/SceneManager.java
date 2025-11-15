package com.complaint.system.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SceneManager {
    private static final Logger logger = LoggerFactory.getLogger(SceneManager.class);
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(true);
        primaryStage.setMaximized(false);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
    }

    public static void loadScene(String fxmlFile) {
        try {
            var fxmlUrl = SceneManager.class.getResource("/fxml/" + fxmlFile);
            if (fxmlUrl == null) {
                logger.error("FXML not found: {}", fxmlFile);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // FORCE CSS RELOAD - Clear any cached stylesheets
            scene.getStylesheets().clear();
            
            // Load CSS with absolute path
            var cssUrl = SceneManager.class.getResource("/css/styles.css");
            if (cssUrl != null) {
                String cssPath = cssUrl.toExternalForm();
                scene.getStylesheets().add(cssPath);
                logger.info("CSS loaded successfully: {}", cssPath);
            } else {
                logger.warn("CSS file not found at /css/styles.css");
            }
            
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            
        } catch (IOException e) {
            logger.error("Error loading scene: {}", fxmlFile, e);
        }
    }

    public static void loadScene(String fxmlFile, String title) {
        loadScene(fxmlFile);
        if (primaryStage != null) {
            primaryStage.setTitle(title);
            primaryStage.setResizable(true);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}