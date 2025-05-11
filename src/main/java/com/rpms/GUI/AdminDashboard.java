package com.rpms.GUI;

import com.rpms.Main;
import com.rpms.UserManagement.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;

public class AdminDashboard extends Application {


    @Override
    public void start(Stage primaryStage) {
        // title
        primaryStage.setTitle("Administrator Dashboard");

        // create tabpane
        TabPane tabPane = new TabPane();

        // create tabs for doctors, patients, admins, and logs
        Tab doctorsTab = new Tab("Doctors", createDoctorsTab());
        doctorsTab.setClosable(false);
        Tab patientsTab = new Tab("Patients", createPatientsTab());
        patientsTab.setClosable(false);
        Tab adminsTab = new Tab("Administrators", createAdminTab());
        adminsTab.setClosable(false);
        Tab logsTab = new Tab("System Logs", createLogsTab());
        logsTab.setClosable(false);



        // add tabs to tabpane
        tabPane.getTabs().addAll(doctorsTab, patientsTab, adminsTab, logsTab);

        // Create Logout Button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            primaryStage.close(); // Close the current dashboard
            Main.logout(); // Call the logout method
        });
         // Add Logout Button to Layout
        VBox layout = new VBox(10, logoutButton, tabPane);
        layout.setPadding(new Insets(10));
        // set up the scene
        Scene scene = new Scene(layout, 800, 600);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);  // Force JVM shutdown
        });
        // Replace the line causing the error (around line 304)
        String cssPath = com.rpms.Main.getStylesheetPath();
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }
        
        primaryStage.show();
    }

    private VBox createDoctorsTab() {
        // create a table to display doctors
        TableView<Doctor> table = new TableView<>();
        // set up the table columns
        table.setItems(FXCollections.observableArrayList(Administrator.getDoctors()));

        // create columns for ID, name, and remove button
        TableColumn<Doctor, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Doctor, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        // create a remove button for each doctor
        TableColumn<Doctor, Void> removeCol = new TableColumn<>("Remove");
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setOnAction(e -> {
                    Doctor doctor = getTableView().getItems().get(getIndex());
                    Administrator.removeDoctor(doctor);
                    Administrator.addSystemLog("Removed doctor: " + doctor.getName());
                    table.setItems(javafx.collections.FXCollections.observableArrayList(Administrator.getDoctors()));
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        // add columns to the table
        java.util.Collections.addAll(table.getColumns(), idCol, nameCol, removeCol);

        TextField nameField = new TextField(); nameField.setPromptText("Name");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone");
        TextField emailField = new TextField(); emailField.setPromptText("Email");
        TextField usernameField = new TextField(); usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password");

        Button addBtn = new Button("Register Doctor");
        addBtn.setOnAction(e -> {
            Doctor doctor = new Doctor("doc" + (Administrator.getDoctors().size() + 1),
                    nameField.getText(), phoneField.getText(), emailField.getText(),
                    usernameField.getText(), passwordField.getText());
            Administrator.registerDoctor(doctor);
            Administrator.addSystemLog("Registered doctor: " + doctor.getName());
            table.setItems(javafx.collections.FXCollections.observableArrayList(Administrator.getDoctors()));
            nameField.clear(); phoneField.clear(); emailField.clear(); usernameField.clear(); passwordField.clear();
        });

        VBox layout = new VBox(10, table, nameField, phoneField, emailField, usernameField, passwordField, addBtn);
        layout.setPadding(new javafx.geometry.Insets(10));
        return layout;
    }

    private VBox createPatientsTab() {
        TableView<Patient> table = new TableView<>();
        table.setItems(FXCollections.observableArrayList(Administrator.getPatients()));

        TableColumn<Patient, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Patient, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Patient, Void> removeCol = new TableColumn<>("Remove");
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setOnAction(e -> {
                    Patient patient = getTableView().getItems().get(getIndex());
                    Administrator.removePatient(patient);
                    Administrator.addSystemLog("Removed patient: " + patient.getName());
                    table.setItems(FXCollections.observableArrayList(Administrator.getPatients()));
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        java.util.Collections.addAll(table.getColumns(), idCol, nameCol, removeCol);

        // Input fields
        TextField nameField = new TextField(); nameField.setPromptText("Name");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone");
        TextField emailField = new TextField(); emailField.setPromptText("Email");
        TextField usernameField = new TextField(); usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password");
        TextField emergencyContactsField = new TextField(); emergencyContactsField.setPromptText("Emergency Contacts (comma-separated)");

        ComboBox<Doctor> physicianComboBox = new ComboBox<>();
        physicianComboBox.setPromptText("Select Physician");
        physicianComboBox.setItems(FXCollections.observableArrayList(Administrator.getDoctors()));

        Button addBtn = new Button("Register Patient");
        addBtn.setOnAction(e -> {
            ArrayList<String> contacts = new ArrayList<>();
            for (String contact : emergencyContactsField.getText().split(",")) {
                if (!contact.trim().isEmpty()) {
                    contacts.add(contact.trim());
                }
            }

            Doctor selectedDoctor = physicianComboBox.getValue();
            if (selectedDoctor == null) {
                showAlert("Please select a physician.");
                return;
            }

            Patient patient = new Patient(
                    "pat" + (Administrator.getPatients().size() + 1),
                    nameField.getText(), phoneField.getText(), emailField.getText(),
                    usernameField.getText(), passwordField.getText(),
                    contacts, selectedDoctor
            );

            Administrator.registerPatient(patient);
            Administrator.addSystemLog("Registered patient: " + patient.getName());
            table.setItems(FXCollections.observableArrayList(Administrator.getPatients()));
            nameField.clear(); phoneField.clear(); emailField.clear(); usernameField.clear(); passwordField.clear();
            emergencyContactsField.clear(); physicianComboBox.getSelectionModel().clearSelection();
        });

        VBox layout = new VBox(10,
                table,
                nameField, phoneField, emailField,
                usernameField, passwordField,
                emergencyContactsField, physicianComboBox,
                addBtn
        );
        layout.setPadding(new javafx.geometry.Insets(10));
        return layout;
    }

    private VBox createAdminTab() {
        // create a table to display administrators
        TableView<Administrator> table = new TableView<>();
        // set up the table columns
        table.setItems(FXCollections.observableArrayList(Administrator.getAdministrators()));

        // create columns for ID, name, and remove button
        TableColumn<Administrator, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Administrator, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        // create a remove button for each administrator
        TableColumn<Administrator, Void> removeCol = new TableColumn<>("Remove");
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setOnAction(e -> {
                    Administrator admin = getTableView().getItems().get(getIndex());
                    Administrator.removeAdministrator(admin);
                    Administrator.addSystemLog("Removed administrator: " + admin.getName());
                    table.setItems(FXCollections.observableArrayList(Administrator.getAdministrators()));
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });

        // add columns to the table
        java.util.Collections.addAll(table.getColumns(), idCol, nameCol, removeCol);

        TextField nameField = new TextField(); nameField.setPromptText("Name");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone");
        TextField emailField = new TextField(); emailField.setPromptText("Email");
        TextField usernameField = new TextField(); usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField(); passwordField.setPromptText("Password");

        Button addBtn = new Button("Register Administrator");
        addBtn.setOnAction(e -> {
            Administrator admin = new Administrator("adm" + (Administrator.getAdministrators().size() + 1),
                    nameField.getText(), phoneField.getText(), emailField.getText(),
                    usernameField.getText(), passwordField.getText());
            Administrator.registerAdministrator(admin);
            Administrator.addSystemLog("Registered administrator: " + admin.getName());
            table.setItems(FXCollections.observableArrayList(Administrator.getAdministrators()));
            nameField.clear(); phoneField.clear(); emailField.clear(); usernameField.clear(); passwordField.clear();
        });

        VBox layout = new VBox(10, table, nameField, phoneField, emailField, usernameField, passwordField, addBtn);
        layout.setPadding(new javafx.geometry.Insets(10));
        return layout;
    }


    private VBox createLogsTab() {
        ListView<String> logsView = new ListView<>();
        if (Administrator.getSystemLogs() != null)
            logsView.setItems(FXCollections.observableArrayList(Administrator.getSystemLogs()));
        else
            Administrator.getSystemLogs().add("No logs yet.");

        Button clearLogsBtn = new Button("Clear Logs");
        clearLogsBtn.setOnAction(e -> {
            Administrator.clearSystemLogs();
            logsView.setItems(FXCollections.observableArrayList(Administrator.getSystemLogs()));
        });

        VBox layout = new VBox(10, new Label("System Logs:"), logsView, clearLogsBtn);
        layout.setPadding(new javafx.geometry.Insets(10));
        return layout;
    }

    // helper method
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
