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
            scene.getStylesheets().add(SceneManager.class.getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            logger.error("Error loading scene: {}", fxmlFile, e);
        }
    }

    public static void loadScene(String fxmlFile, String title) {
        loadScene(fxmlFile);
        if (primaryStage != null) {
            primaryStage.setTitle(title);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}