// GUI/PatientDashboard.java
package com.rpms.GUI;

import com.rpms.Main;
import com.rpms.AppointmentScheduling.Appointment;
import com.rpms.ChatAndVideoConsultation.VideoCall;
import com.rpms.UserManagement.*;
import com.rpms.HealthDataHandling.VitalSign;
import com.rpms.NotificationsAndReminders.ReminderService;
import com.rpms.Reports.ReportGenerator;
import com.rpms.DoctorPatientInteraction.Feedback;
import com.rpms.ChatAndVideoConsultation.ChatManager;

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
import java.util.List;

/**
 * Dashboard interface for patient users.
 * Provides functionality to manage vital signs, appointments, emergency contacts,
 * view medical feedback, and communicate with doctors.
 */
public class PatientDashboard {

    /** The patient user who is currently logged in */
    private Patient patient;

    /** Flag to track if reminders have been shown during this session */
    private boolean remindersShown = false; // for keeping track of alerts

    /**
     * Creates a new patient dashboard for the specified patient.
     * 
     * @param patient The patient user who logged in
     */
    public PatientDashboard(Patient patient) {
        this.patient = patient;
    }

    /**
     * Initializes and displays the patient dashboard interface.
     * Sets up all tabs, components, and event handlers.
     * 
     * @param stage The JavaFX stage to display the dashboard
     */
    public void start(Stage stage) {
        stage.setTitle("Patient Dashboard - " + patient.getName());

        BorderPane root = new BorderPane();



        // Panic button on top right
        Button panicBtn = new Button("Panic");
        panicBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Enter emergency message");
            dialog.showAndWait().ifPresent(message -> {
                patient.panicButton(message);
                showAlert("Panic Alert", "Panic button pressed with message: " + message);
            });
        });


        // Logout button on top left
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            stage.close(); // Close the current dashboard
            Main.logout(); // Call the logout method
        });


        HBox topBar = new HBox(10, logoutBtn, panicBtn);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(10));
        root.setTop(topBar);

        TabPane tabPane = new TabPane();

        // ===== VITALS TAB =====
        Tab vitalsTab = new Tab("Vitals");
        vitalsTab.setClosable(false);

        VBox vitalsLayout = new VBox(10);
        vitalsLayout.setPadding(new Insets(10));

        for (VitalSign vital : patient.viewPreviousVitals()) {
            HBox row = new HBox(10);
            Label vitalLabel = new Label(vital.toString());
            Button removeBtn = new Button("Remove");
            removeBtn.setOnAction(e -> {
                patient.removeVital(vital);
                showAlert("Vital Removed", vital.toString());
                start(stage);
            });
            row.getChildren().addAll(vitalLabel, removeBtn);
            vitalsLayout.getChildren().add(row);
        }

        Button uploadVitals = new Button("Upload Vitals (CSV)");
        uploadVitals.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Vital Signs CSV");
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                List<String> alerts = patient.uploadVitalsFromCSV(file.getAbsolutePath());
                showAlert("Vitals Uploaded", "Vitals uploaded from: " + file.getName());
                if (!alerts.isEmpty()) {
                    showAlert("Critical Vitals Detected", String.join("\n", alerts));
                }
                start(stage);
            }
        });

        Button graphVitalsBtn = new Button("Show Vitals Graph");
        graphVitalsBtn.setOnAction(e -> patient.getVitals().generateVitalsGraph(new Stage()));

        vitalsLayout.getChildren().addAll(uploadVitals, graphVitalsBtn);
        vitalsTab.setContent(new ScrollPane(vitalsLayout));

        // ===== APPOINTMENTS TAB =====
        Tab appointmentsTab = new Tab("Appointments");
        appointmentsTab.setClosable(false);
        VBox appointmentLayout = new VBox(10);
        appointmentLayout.setPadding(new Insets(10));

        for (Appointment appt : patient.viewAppointments()) {
            HBox row = new HBox(10);
            Label apptLabel = new Label(appt.toString());
            Button cancelBtn = new Button("Cancel");
            cancelBtn.setOnAction(e -> {
                boolean removed = patient.cancelAppointment(appt);
                showAlert("Appointment", removed ? "Cancelled" : "Failed to cancel");
                start(stage);
            });
            row.getChildren().addAll(apptLabel, cancelBtn);
            appointmentLayout.getChildren().add(row);
        }

        Button requestAppt = new Button("Request Appointment");
        requestAppt.setOnAction(e -> {
            TextInputDialog dateDialog = new TextInputDialog("2025-05-10T15:30");
            dateDialog.setTitle("Request Appointment");
            dateDialog.setHeaderText("Enter Appointment Date & Time (e.g. 2025-05-10T15:30)");
            dateDialog.showAndWait().ifPresent(dateInput -> {
                try {
                    LocalDateTime dateTime = LocalDateTime.parse(dateInput);
                    List<Doctor> doctors = Administrator.getDoctors();
                    ChoiceDialog<Doctor> doctorDialog = new ChoiceDialog<>(doctors.get(0), doctors);
                    doctorDialog.setTitle("Select Doctor");
                    doctorDialog.setHeaderText("Choose a doctor for the appointment");
                    doctorDialog.showAndWait().ifPresent(selectedDoctor -> {
                        Appointment appt = new Appointment(dateTime, selectedDoctor, patient);
                        patient.requestAppointment(appt);
                        showAlert("Appointment Requested", appt.toString());
                        start(stage);
                    });
                } catch (Exception ex) {
                    showAlert("Invalid Input", "Invalid date-time format.");
                }
            });
        });

        Button requestVideoCall = new Button("Request Video Call");
        requestVideoCall.setOnAction(e -> {
            TextInputDialog startDialog = new TextInputDialog("2025-05-10T15:30");
            startDialog.setTitle("Request Video Call");
            startDialog.setHeaderText("Enter Start Date & Time (e.g. 2025-05-10T15:30)");
            startDialog.showAndWait().ifPresent(startInput -> {
                try {
                    LocalDateTime startTime = LocalDateTime.parse(startInput);

                    TextInputDialog endDialog = new TextInputDialog("2025-05-10T16:30");
                    endDialog.setTitle("Request Video Call");
                    endDialog.setHeaderText("Enter End Date & Time (e.g. 2025-05-10T16:30)");
                    endDialog.showAndWait().ifPresent(endInput -> {
                        try {
                            LocalDateTime endTime = LocalDateTime.parse(endInput);
                            List<Doctor> doctors = Administrator.getDoctors();
                            ChoiceDialog<Doctor> doctorDialog = new ChoiceDialog<>(doctors.get(0), doctors);
                            doctorDialog.setTitle("Select Doctor");
                            doctorDialog.setHeaderText("Choose a doctor for the video call");
                            doctorDialog.showAndWait().ifPresent(selectedDoctor -> {
                                VideoCall videocall = new VideoCall(selectedDoctor,patient, startTime, endTime);
                                patient.requestVideoCall(videocall);
                                showAlert("Video Call Requested", "Video call scheduled with " + selectedDoctor.getName());
                                start(stage);
                            });
                        } catch (Exception ex) {
                            showAlert("Invalid Input", "Invalid end date-time format.");
                        }
                    });
                } catch (Exception ex) {
                    showAlert("Invalid Input", "Invalid start date-time format.");
                }
            });
        });


        appointmentLayout.getChildren().addAll(requestAppt, requestVideoCall);

        // Divider label
        Label videoCallLabel = new Label("Scheduled Video Calls");
        videoCallLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-underline: true;");
        appointmentLayout.getChildren().add(videoCallLabel);

        // List video calls
        for (VideoCall vc : patient.viewVideoCalls()) {
            HBox vcRow = new HBox(10);
            vcRow.setAlignment(Pos.CENTER_LEFT);
            Label vcLabel = new Label(vc.toString());
            Button cancelVCBtn = new Button("Cancel");
            cancelVCBtn.setOnAction(e -> {
                boolean removed = patient.cancelVideoCall(vc);
                showAlert("Video Call", removed ? "Cancelled" : "Failed to cancel");
                start(stage);
            });
            vcRow.getChildren().addAll(vcLabel, cancelVCBtn);
            appointmentLayout.getChildren().add(vcRow);
        }

        appointmentsTab.setContent(new ScrollPane(appointmentLayout));

        // ===== EMERGENCY CONTACTS TAB =====
        Tab contactTab = new Tab("Emergency Contacts");
        contactTab.setClosable(false);
        VBox contactLayout = new VBox(10);
        contactLayout.setPadding(new Insets(10));

        for (String contact : patient.getEmergencyContacts()) {
            HBox row = new HBox(10);
            Label contactLabel = new Label(contact);
            Button removeBtn = new Button("Remove");
            removeBtn.setOnAction(e -> {
                patient.removeEmergencyContact(contact);
                showAlert("Contact Removed", contact);
                start(stage);
            });
            row.getChildren().addAll(contactLabel, removeBtn);
            contactLayout.getChildren().add(row);
        }

        Button addContactBtn = new Button("Add Contact");
        addContactBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Enter contact number to add");
            dialog.showAndWait().ifPresent(contact -> {
                patient.addEmergencyContact(contact);
                showAlert("Contact Added", contact);
                start(stage);
            });
        });
        contactLayout.getChildren().add(addContactBtn);
        contactTab.setContent(new ScrollPane(contactLayout));

        // ===== FEEDBACKS TAB =====
        Tab feedbackTab = new Tab("Feedbacks");
        feedbackTab.setClosable(false);
        VBox fbLayout = new VBox(10);
        fbLayout.setPadding(new Insets(10));

        for (Feedback fb : patient.viewPreviousFeedbacks()) {
            fbLayout.getChildren().add(new Label(fb.toString()));
        }

        Button downloadReport = new Button("Download Report");
        downloadReport.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Patient Report");
            fileChooser.setInitialFileName("Patient_" + patient.getId() + "_Report.txt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File selectedFile = fileChooser.showSaveDialog(stage);
            if (selectedFile != null) {
                ReportGenerator.generatePatientReport(patient, selectedFile);
                showAlert("Report Saved", selectedFile.getAbsolutePath());
            }
        });
        fbLayout.getChildren().add(downloadReport);
        feedbackTab.setContent(new ScrollPane(fbLayout));
        
        // ===== CHAT TAB =====
        Tab chatTab = new Tab("Chat");
        chatTab.setClosable(false);
        VBox chatLayout = new VBox(10);
        chatLayout.setPadding(new Insets(10));
        
        Label chatLabel = new Label("Chat with your doctors");
        chatLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        chatLayout.getChildren().add(chatLabel);
        
        // Get all doctors this patient can chat with
        List<User> availableDoctors = ChatManager.getChatContactsForUser(patient);
        
        if (!availableDoctors.isEmpty()) {
            // Create a section for the primary physician
            Doctor physician = patient.getPhysician();
            if (physician != null) {
                HBox primaryDocRow = new HBox(10);
                primaryDocRow.setAlignment(Pos.CENTER_LEFT);
                primaryDocRow.setPadding(new Insets(5));
                primaryDocRow.setStyle("-fx-border-color: #2a9d8f; -fx-border-radius: 5; -fx-background-color: #e9f5f3;");
                
                Label primaryLabel = new Label("Primary Physician:");
                primaryLabel.setStyle("-fx-font-weight: bold;");
                
                Label doctorLabel = new Label("Dr. " + physician.getName());
                doctorLabel.setPrefWidth(200);
                
                Button openChatBtn = new Button("Open Chat");
                openChatBtn.setOnAction(e -> {
                    ChatWindow chatWindow = new ChatWindow(patient, physician);
                    chatWindow.show();
                });
                
                primaryDocRow.getChildren().addAll(primaryLabel, doctorLabel, openChatBtn);
                chatLayout.getChildren().add(primaryDocRow);
            }
            
            // Add a separator and header for other doctors
            if (availableDoctors.size() > 1) {
                Separator separator = new Separator();
                separator.setPadding(new Insets(5, 0, 5, 0));
                
                Label otherDoctorsLabel = new Label("Other Available Doctors:");
                otherDoctorsLabel.setStyle("-fx-font-weight: bold;");
                
                chatLayout.getChildren().addAll(separator, otherDoctorsLabel);
                
                // List all other doctors
                for (User user : availableDoctors) {
                    if (user instanceof Doctor doctor && (physician == null || !doctor.equals(physician))) {
                        HBox docRow = new HBox(10);
                        docRow.setAlignment(Pos.CENTER_LEFT);
                        docRow.setPadding(new Insets(5));
                        docRow.setStyle("-fx-border-color: #eee; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
                        
                        Label docNameLabel = new Label("Dr. " + doctor.getName());
                        docNameLabel.setPrefWidth(200);
                        
                        Button chatBtn = new Button("Open Chat");
                        chatBtn.setOnAction(e -> {
                            ChatWindow chatWindow = new ChatWindow(patient, doctor);
                            chatWindow.show();
                        });
                        
                        docRow.getChildren().addAll(docNameLabel, chatBtn);
                        chatLayout.getChildren().add(docRow);
                    }
                }
            }
        } else {
            chatLayout.getChildren().add(new Label("You don't have any doctors available for chat."));
        }
        
        chatTab.setContent(new ScrollPane(chatLayout));

        tabPane.getTabs().addAll(vitalsTab, appointmentsTab, contactTab, feedbackTab, chatTab);
        root.setCenter(tabPane);

        // Show reminders as alert
        if (!remindersShown) {
            ReminderService reminderService = new ReminderService(patient);
            String appointments = reminderService.getAppointmentReminders();
            String medications = reminderService.getMedicationReminders();
            String videoCalls = reminderService.getApprovedVideoCalls();
            StringBuilder reminderMsg = new StringBuilder();
            if (!appointments.isEmpty()) reminderMsg.append("Upcoming Appointments:\n").append(appointments);
            if (!medications.isEmpty()) reminderMsg.append("Medications:\n").append(medications);
            if (!videoCalls.isEmpty()) reminderMsg.append("Upcoming Video Calls:\n").append(videoCalls);
            if (reminderMsg.length() > 0) showAlert("Reminders", reminderMsg.toString());
            remindersShown = true;
        }
        Scene scene = new Scene(root, 900, 600);

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
     * Displays an alert dialog with the specified title and message.
     * 
     * @param title The title of the alert dialog
     * @param message The message to display in the alert
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(500, 300);
        alert.showAndWait();
    }
}
