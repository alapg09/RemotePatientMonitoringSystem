package com.rpms;

import com.rpms.ChatAndVideoConsultation.ChatManager;
import com.rpms.GUI.LoginScreen;
import javafx.application.Application;
import javafx.application.Platform;

/**
 * NOTE: 
 * All the javadocs are added with the help of Github Copilot.
 */

/**
 * Main entry point for the Remote Patient Monitoring System (RPMS) application.
 * This class initializes the chat server and launches the JavaFX application.
 */
public class Main {
    /** Path to the CSS file for consistent styling across all screens */
    public static final String CSS_PATH = "/css/minimal.css";
    
    /**
     * Main method that starts the RPMS application.
     * Initializes the chat server and launches the login screen.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Add shutdown hook OUTSIDE the try block to ensure it's registered early
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Shutting down chat server...");
                ChatManager.stopServer();
                System.out.println("Chat server stopped successfully.");
            } catch (Exception e) {
                System.err.println("Error stopping chat server: " + e.getMessage());
            }
        }));

        try {
            // Try to release the port if it's in use
            ChatManager.forceReleasePort();

            // Start the chat server
            ChatManager.startServer();
            
            // Initialize application styling
            initializeAppStyling();

            // Add a handler for application exit
            Platform.setImplicitExit(true);

            // Launch the JavaFX application via LoginScreen
            Application.launch(LoginScreen.class, args);
        } catch (Exception e) {
            System.err.println("Error in main application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initializes styling for the application.
     * This ensures CSS resources are preloaded.
     */
    private static void initializeAppStyling() {
        try {
            // Ensure the CSS resource exists and is accessible
            if (Main.class.getResource(CSS_PATH) != null) {
                System.out.println("Successfully loaded application CSS: " + CSS_PATH);
            } else {
                System.err.println("Warning: Could not find CSS file: " + CSS_PATH);
            }
            
            // You could preload fonts here too if needed
            // Font.loadFont(Main.class.getResourceAsStream("/fonts/Roboto-Regular.ttf"), 14);
        } catch (Exception e) {
            System.err.println("Error initializing application styling: " + e.getMessage());
        }
    }
    
    /**
     * Gets the CSS stylesheet resource URI that can be used by any screen.
     * 
     * @return String containing the CSS resource URI or null if resource not found
     */
    public static String getStylesheetPath() {
        java.net.URL resource = Main.class.getResource(CSS_PATH);
        if (resource != null) {
            return resource.toExternalForm();
        } else {
            System.err.println("Warning: CSS file not found at " + CSS_PATH);
            return null;
        }
    }
    public static void logout() {
        Platform.runLater(() -> {
            try {
                System.out.println("Logging out...");
                new LoginScreen().start(new javafx.stage.Stage());
            } catch (Exception e) {
                System.err.println("Error during logout: " + e.getMessage());
            }
        });
    }
    
}