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

public class LoginScreen extends Application {

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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // Authenticates the user
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

    // Modify the init() method to use our DataManager
    @Override
    public void init() throws Exception {
        // Try to load data automatically
        DataManager.loadAllData();

//         If no data was loaded (first run), create sample data
        if (Administrator.getPatients().isEmpty() && Administrator.getDoctors().isEmpty()) {
            createSampleData();
        }
    }

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

    @Override
    public void stop() throws Exception {
        System.out.println("Application is closing. Saving all data...");
        DataManager.saveAllData();
        System.out.println("Data saved successfully.");
        super.stop();
    }

}
