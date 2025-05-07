package com.rpms;

import com.rpms.ChatAndVideoConsultation.ChatManager;
import com.rpms.GUI.LoginScreen;
import javafx.application.Application;
import javafx.application.Platform;


/**
 * NOTE: 
 * All the javadocs are added using the help of Github Copilot.
 * /


/**
 * Main entry point for the Remote Patient Monitoring System (RPMS) application.
 * This class initializes the chat server and launches the JavaFX application.
 */
public class Main {
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

            // Add a handler for application exit
            Platform.setImplicitExit(true);

            // Launch the JavaFX application via LoginScreen
            Application.launch(LoginScreen.class, args);
        } catch (Exception e) {
            System.err.println("Error in main application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}