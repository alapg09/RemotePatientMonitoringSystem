package com.rpms.GUI;

import com.rpms.Main;
import com.rpms.AppointmentScheduling.Appointment;
import com.rpms.ChatAndVideoConsultation.VideoCall;
import com.rpms.DoctorPatientInteraction.Feedback;
import com.rpms.DoctorPatientInteraction.Prescription;
import com.rpms.HealthDataHandling.VitalSign;
import com.rpms.Reports.ReportGenerator;
import com.rpms.UserManagement.Doctor;
import com.rpms.UserManagement.Patient;
import com.rpms.UserManagement.Administrator;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Dashboard interface for doctor users.
 * Provides functionality to manage patients, view and approve appointments,
 * give medical feedback, and engage in video consultations.
 */
public class DoctorDashboard {

    /** The doctor user who is currently logged in */
    private Doctor doctor;

    /** Flag to track if reminders have been shown during this session */
    private boolean remindersShown = false; // for keeping track of alerts

    /**
     * Creates a new doctor dashboard for the specified doctor.
     * 
     * @param doctor The doctor user who logged in
     */
    public DoctorDashboard(Doctor doctor) {
        this.doctor = doctor;
    }

    /**
     * Initializes and displays the doctor dashboard interface.
     * Sets up all tabs, components, and event handlers.
     * 
     * @param stage The JavaFX stage to display the dashboard
     */
    public void start(Stage stage) {
        // Refresh patient data from Administrator's list to ensure fresh data
        ArrayList<Patient> refreshedPatients = new ArrayList<>();
        for (Patient p : doctor.getPatients()) {
            for (Patient adminPatient : Administrator.getPatients()) {
                if (p.getId().equals(adminPatient.getId())) {
                    refreshedPatients.add(adminPatient); // Use the version from Administrator
                    break;
                }
            }
        }
        

        // Set the refreshed list back to the doctor
        doctor.getPatients().clear();
        doctor.getPatients().addAll(refreshedPatients);

        // Now continue with the original code
        stage.setTitle("Doctor Dashboard - " + doctor.getName());

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // === PATIENT MANAGEMENT TAB ===
        Tab patientTab = new Tab("Patient Management");
        VBox patientContainer = new VBox(10);
        patientContainer.setPadding(new Insets(10));

        for (Patient patient : doctor.getPatients()) {
            HBox patientBox = new HBox(10);
            patientBox.setPadding(new Insets(10));
            patientBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
            Label nameLabel = new Label(patient.toString());

            Button viewVitalsBtn = new Button("View Vitals");
            viewVitalsBtn.setOnAction(e -> showVitalsPopup(patient));

            Button downloadReportBtn = new Button("Download Report");
            downloadReportBtn.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save Patient Report");
                fileChooser.setInitialFileName(patient.getName() + "_Report.txt");
                File file = fileChooser.showSaveDialog(null);
                if (file != null) {
                    ReportGenerator.generatePatientReport(patient, file);
                }
            });

            Button feedbackBtn = new Button("Give Feedback");
            feedbackBtn.setOnAction(e -> showFeedbackDialog(patient));

            Button graphBtn = new Button("View Vitals Graph");
            graphBtn.setOnAction(e -> patient.getVitals().generateVitalsGraph(new Stage()));
            
            // Add new chat button
            Button chatBtn = new Button("Chat");
            chatBtn.setOnAction(e -> {
                ChatWindow chatWindow = new ChatWindow(doctor, patient);
                chatWindow.show();
            });

            patientBox.getChildren().addAll(nameLabel, viewVitalsBtn, graphBtn, downloadReportBtn, feedbackBtn, chatBtn);
            patientContainer.getChildren().add(patientBox);
        }

        if (doctor.getPatients().isEmpty()) {
            patientContainer.getChildren().add(new Label("No patients assigned."));
        }

        ScrollPane patientScroll = new ScrollPane(patientContainer);
        patientScroll.setFitToWidth(true);
        patientTab.setContent(patientScroll);

        // === APPOINTMENTS & VIDEO CALLS TAB ===
        Tab appointmentsTab = new Tab("Appointments & Video Calls");

        VBox tabContent = new VBox(20);
        tabContent.setPadding(new Insets(10));

        // Appointments Section
        VBox appointmentsBox = new VBox(10);
        appointmentsBox.getChildren().add(new Label("Pending/Upcoming Appointments:"));

        if (doctor.getAppointments().isEmpty()) {
            appointmentsBox.getChildren().add(new Label("No appointments available."));
        } else {
            for (Appointment appt : doctor.getPendingAndApprovedAppointments()) {
                HBox apptBox = new HBox(10);
                apptBox.setAlignment(Pos.CENTER_LEFT);
                apptBox.setStyle("-fx-border-color: lightgray; -fx-padding: 5;");
                Label info = new Label(appt.toString());

                Button approveBtn = new Button("Approve");
                approveBtn.setOnAction(e -> {
                    doctor.approveAppointment(appt);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment approved.");
                    alert.show();
                    start(stage); // refresh
                });

                Button cancelBtn = new Button("Reject");
                cancelBtn.setOnAction(e -> {
                    doctor.cancelAppointment(appt);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Appointment cancelled.");
                    alert.show();
                    start(stage); // refresh
                });

                switch(appt.getStatus()) {
                    case "Cancelled":
                        approveBtn.setDisable(true);
                        cancelBtn.setDisable(true);
                        break;
                    case "Pending":
                        cancelBtn.setText("Reject");
                        break;
                    case "Approved":
                        approveBtn.setDisable(true);
                        cancelBtn.setText("Cancel");
                        break;

                }
                apptBox.getChildren().addAll(info, approveBtn, cancelBtn);
                appointmentsBox.getChildren().add(apptBox);
            }
        }

        ScrollPane apptScroll = new ScrollPane(appointmentsBox);
        apptScroll.setFitToWidth(true);

        // Video Call Section
        VBox videoCallBox = new VBox(10);
        videoCallBox.getChildren().add(new Label("Video Calls:"));

        if (doctor.getVideoCalls().isEmpty()) {
            videoCallBox.getChildren().add(new Label("No video calls available."));
        } else {
            for (VideoCall call : doctor.getVideoCalls()) {
                VBox callInfo = new VBox(5);
                callInfo.setStyle("-fx-border-color: lightgray; -fx-padding: 10;");
                Label details = new Label(call.toString());

                Button approveBtn = new Button("Approve");
                approveBtn.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog("https://meet.google.com/");
                    dialog.setTitle("Enter Google Meet Link");
                    dialog.setHeaderText("Approve Video Call");
                    dialog.setContentText("Enter the Google Meet link:");

                    Optional<String> result = dialog.showAndWait();
                    result.ifPresent(link -> {
                        call.setLink(link);
                        doctor.approveVideoCall(call);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Video call approved with link:\n" + link);
                        alert.show();
                        start(stage); // refresh
                    });
                });

                Button cancelBtn = new Button("Reject");
                cancelBtn.setOnAction(e -> {
                    doctor.cancelVideoCall(call);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Video call cancelled.");
                    alert.show();
                    start(stage); // refresh
                });

                // Handle status-based UI
                switch (call.getStatus()) {
                    case "Pending":
                        cancelBtn.setText("Reject");
                        break;
                    case "Approved":
                        approveBtn.setDisable(true);
                        cancelBtn.setText("Cancel");
                        break;
                }

                callInfo.getChildren().addAll(details, approveBtn, cancelBtn);
                videoCallBox.getChildren().add(callInfo);
            }
        }


        ScrollPane videoScroll = new ScrollPane(videoCallBox);
        videoScroll.setFitToWidth(true);

        tabContent.getChildren().addAll(apptScroll, videoScroll);
        appointmentsTab.setContent(tabContent);
        
        // === CHAT TAB ===
        Tab chatTab = new Tab("Patient Chats");
        VBox chatContainer = new VBox(10);
        chatContainer.setPadding(new Insets(10));
        
        Label chatHeaderLabel = new Label("Chat with your patients");
        chatHeaderLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        chatContainer.getChildren().add(chatHeaderLabel);
        
        // List all patients with chat buttons
        for (Patient patient : doctor.getPatients()) {
            HBox patientChatRow = new HBox(10);
            patientChatRow.setAlignment(Pos.CENTER_LEFT);
            patientChatRow.setPadding(new Insets(5));
            patientChatRow.setStyle("-fx-border-color: #eee; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
            
            Label patientLabel = new Label(patient.getName() + " (ID: " + patient.getId() + ")");
            patientLabel.setPrefWidth(200);
            
            Button openChatBtn = new Button("Open Chat");
            openChatBtn.setOnAction(e -> {
                ChatWindow chatWindow = new ChatWindow(doctor, patient);
                chatWindow.show();
            });
            
            patientChatRow.getChildren().addAll(patientLabel, openChatBtn);
            chatContainer.getChildren().add(patientChatRow);
        }
        
        if (doctor.getPatients().isEmpty()) {
            chatContainer.getChildren().add(new Label("No patients to chat with."));
        }
        
        ScrollPane chatScroll = new ScrollPane(chatContainer);
        chatScroll.setFitToWidth(true);
        chatTab.setContent(chatScroll);

        // === Add Tabs to TabPane ===
        tabPane.getTabs().addAll(patientTab, appointmentsTab, chatTab);

        // Create Logout Button
        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            stage.close(); // Close the current dashboard
            Main.logout(); // Call the logout method
        });

        // Add Logout Button to Layout
        VBox layout = new VBox(10, logoutButton, tabPane);
        layout.setPadding(new Insets(10));


        // Only show critical vitals alert if it hasn't been shown yet in this session
        if (!remindersShown) {
            String msg = doctor.patientCriticalVitalDetection();
            if (!msg.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, msg);
                alert.setTitle("Critical Vitals Detected");
                alert.setHeaderText("Critical Vitals Detected!");
                alert.showAndWait();
            }
            // Mark that reminders have been shown for this session
            remindersShown = true;
        }

        Scene scene = new Scene(layout, 900, 600);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);  // Force JVM shutdown
        });
        // Replace the line causing the error (around line 304)
        String cssPath = com.rpms.Main.getStylesheetPath();
        if (cssPath != null) {
            scene.getStylesheets().add(cssPath);
        }

        stage.show();
    }

    /**
     * Displays a popup window showing all vital signs for a patient.
     * 
     * @param patient The patient whose vital signs to display
     */
    private void showVitalsPopup(Patient patient) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Vitals - " + patient.getName());
        alert.setHeaderText("Vitals History");

        StringBuilder vitalsText = new StringBuilder();
        ArrayList<VitalSign> vitals = patient.getVitals().getVitals();

        System.out.println("DEBUG: Attempting to show vitals for patient: " + patient.getName());
        System.out.println("DEBUG: Vitals list size: " + (vitals != null ? vitals.size() : "null"));

        if (vitals == null || vitals.isEmpty()) {
            vitalsText.append("No vitals recorded for this patient.");
        } else {
            for (VitalSign v : vitals) {
                vitalsText.append(v.toString()).append("\n");
            }
        }

        alert.setContentText(vitalsText.toString());
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(500, 300);
        alert.showAndWait();
    }

    /**
     * Displays a dialog for entering feedback and prescriptions for a patient.
     * 
     * @param patient The patient to provide feedback to
     */
    private void showFeedbackDialog(Patient patient) {
        Dialog<Feedback> dialog = new Dialog<>();
        dialog.setTitle("Give Feedback");

        Label commentLabel = new Label("Comments:");
        TextArea commentArea = new TextArea();

        VBox prescriptionBox = new VBox(5);
        Button addPrescriptionBtn = new Button("Add Prescription");

        ArrayList<Prescription> prescriptions = new ArrayList<>();

        addPrescriptionBtn.setOnAction(e -> {
            Dialog<Prescription> presDialog = new Dialog<>();
            presDialog.setTitle("Add Prescription");

            TextField medField = new TextField();
            TextField dosageField = new TextField();
            TextField scheduleField = new TextField();

            GridPane presGrid = new GridPane();
            presGrid.setHgap(10);
            presGrid.setVgap(10);
            presGrid.setPadding(new Insets(10));

            presGrid.add(new Label("Medication:"), 0, 0);
            presGrid.add(medField, 1, 0);
            presGrid.add(new Label("Dosage:"), 0, 1);
            presGrid.add(dosageField, 1, 1);
            presGrid.add(new Label("Schedule:"), 0, 2);
            presGrid.add(scheduleField, 1, 2);

            presDialog.getDialogPane().setContent(presGrid);
            presDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            presDialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return new Prescription(medField.getText(), dosageField.getText(), scheduleField.getText());
                }
                return null;
            });

            Prescription prescription = presDialog.showAndWait().orElse(null);
            if (prescription != null) {
                prescriptions.add(prescription);
                prescriptionBox.getChildren().add(new Label(prescription.toString()));
            }
        });

        VBox content = new VBox(10, commentLabel, commentArea, addPrescriptionBtn, prescriptionBox);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Feedback(commentArea.getText(), prescriptions, LocalDateTime.now());
            }
            return null;
        });

        Feedback feedback = dialog.showAndWait().orElse(null);
        if (feedback != null) {
            patient.addFeedback(feedback);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Feedback added.");
            alert.show();
        }
    }
}
