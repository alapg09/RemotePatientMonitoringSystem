package com.rpms.GUI;

import com.rpms.utilities.DataManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import com.rpms.UserManagement.*;

/**
 * The initial login screen for all users of the system.
 * Handles authentication and directs users to appropriate dashboards.
 */
public class LoginScreen extends Application {

    /**
     * Initializes and displays the login screen interface.
     * Sets up the GUI components and event handlers.
     * 
     * @param primaryStage The primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Remote Health Monitoring System - Login");

        // Creating GUI elements
        Label roleLabel = new Label("Select role:");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Patient", "Doctor", "Administrator");

        Label usernameLabel = new Label("Username:");
        TextField usernameTextField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20;");
        layout.getChildren().addAll(
                roleLabel, roleComboBox,
                usernameLabel, usernameTextField,
                passwordLabel, passwordField,
                loginButton
        );

        loginButton.setOnAction(e -> {
            String role = roleComboBox.getValue();
            String enteredUsername = usernameTextField.getText();
            String enteredPassword = passwordField.getText();

            if (role == null) {
                showAlert("Please select a role.");
                return;
            }

            User user = authenticateUser(enteredUsername, enteredPassword, role);

            if (user == null) {
                showAlert("Invalid username or password.");
                // Log the failed login attempt
                Administrator.addSystemLog("Failed login attempt for username '" + enteredUsername + "' with role '" + role + "'.");

                return;
            }
            else{
                // Log the successful login
                Administrator.addSystemLog("Successful login for username '" + enteredUsername + "' with role '" + role + "'.");
            }


            // Open the correct dashboard
            switch (role) {
                case "Patient":
                    new PatientDashboard((Patient) user).start(new Stage());
                    break;
                case "Doctor":
                    new DoctorDashboard((Doctor) user).start(new Stage());
                    break;
                case "Administrator":
                    new AdminDashboard().start(new Stage());
                    break;
            }
            primaryStage.close();
        });

        Scene scene = new Scene(layout, 300, 280);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);  // Force JVM shutdown
        });

        primaryStage.show();
    }

    /**
     * Displays an alert dialog with the given message.
     * 
     * @param message The message to display in the alert
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Main method to launch the application directly from this class.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Authenticates a user against the system database.
     * Checks if the provided credentials match any user with the specified role.
     * 
     * @param username The username entered by the user
     * @param password The password entered by the user
     * @param role The selected role (Patient, Doctor, or Administrator)
     * @return The authenticated User object or null if authentication fails
     */
    public User authenticateUser(String username, String password, String role) {
        for (User user : Administrator.getAllUsers()) {
            if (user.checkUsername(username)
                    && user.checkPassword(password)
                    && user.getRole().equals(role)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Initializes the application by loading existing data or creating sample data.
     * This method is called automatically by the JavaFX framework.
     * 
     * @throws Exception If an error occurs during initialization
     */
    @Override
    public void init() throws Exception {
        // Try to load data automatically
        DataManager.loadAllData();

        // If no data was loaded (first run), create sample data
        if (Administrator.getPatients().isEmpty() && Administrator.getDoctors().isEmpty()) {
            createSampleData();
        }
    }

    /**
     * Creates sample data for the system when running for the first time.
     * Adds a default doctor, patient, and administrator.
     */
    private void createSampleData() {
        // same email is used for checking purposes
        Doctor doctor = new Doctor("doc1", "Khurram Shabbir", "+92-316-5668990", "rpms502082.test@gmail.com", "kshabbir.doc", "kshabbir123");
        Patient patient = new Patient("pat1", "Ali Khan", "+92-322-9328676", "rpms502082.test@gmail.com", "akhan", "akhan123", new ArrayList<>(), doctor);
        Administrator admin = new Administrator("adm1", "Alap Gohar", "+92-316-5666994", "alapgohar123@gmail.com", "agohar.adm", "502082.default.adm");

        // Register users
        Administrator.registerPatient(patient);
        Administrator.registerDoctor(doctor);
        Administrator.registerAdministrator(admin);
    }

    /**
     * Clean up resources when the application is stopping.
     * Saves all data before shutting down.
     * 
     * @throws Exception If an error occurs during shutdown
     */
    @Override
    public void stop() throws Exception {
        System.out.println("Application is closing. Saving all data...");
        DataManager.saveAllData();
        System.out.println("Data saved successfully.");
        super.stop();
    }
}
