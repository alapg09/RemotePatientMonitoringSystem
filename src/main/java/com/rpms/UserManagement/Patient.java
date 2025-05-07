package com.rpms.UserManagement;


// required imports
import com.rpms.AppointmentScheduling.Appointment;
import com.rpms.AppointmentScheduling.AppointmentManager;
import com.rpms.ChatAndVideoConsultation.VideoCall;
import com.rpms.DoctorPatientInteraction.Feedback;
import com.rpms.EmergencyAlertSystem.*;
import com.rpms.HealthDataHandling.VitalSign;
import com.rpms.HealthDataHandling.VitalsDatabase;
import com.rpms.utilities.DataManager;


import java.util.ArrayList;
// file i/o
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
// date and time
import java.time.LocalDateTime;

public class Patient extends User {
    // attributes specific to the Patient class
    private final VitalsDatabase vitalsDatabase;  // to store the vital signs of the patient
    private final ArrayList<Feedback> feedbacks;  // to store previous feedbacks given by doctors
    private ArrayList<String> emergencyContacts; // to store emergency contact numbers of the patient
    private Doctor physician;   // this is the general physician of the patient who tracks the vitals etc but the patient can havae multiple doctors for different specializations

    private static final long serialVersionUID = 1L;

    // constructor to initialize the Patient object
    public Patient(String id, String name, String phoneNumber, String email, String username, String password, ArrayList<String> emergencyContacts, Doctor physician) {
        super(id, name, phoneNumber, email, username, password);
        // new vitals database object for each patient4
        this.vitalsDatabase = new VitalsDatabase();
        // new arraylist for feedbacks for each patient
        this.feedbacks = new ArrayList<>();
        // new arraylist for emergency contacts for each patient
        this.emergencyContacts = emergencyContacts;
        // setting the physician for the patient
        this.physician = physician;
        // Safe check before adding
        if (physician != null) {
            physician.addPatient(this);
        }
    }

    @Override
    public String getRole() {
        return "Patient";
    }

    // getter for VitalsDatabase of each patient
    public VitalsDatabase getVitals() {
        return vitalsDatabase;
    }

    // getter for feedback
    public ArrayList<Feedback> getFeedbacks() {
        return feedbacks;
    }
    // getter for emergency contacts
    public ArrayList<String> getEmergencyContacts() {
        return emergencyContacts;
    }
    // setter for emergency contacts
    public void setEmergencyContacts(ArrayList<String> emergencyContacts) {
        this.emergencyContacts = emergencyContacts;
    }
    // getter for physician
    public Doctor getPhysician() {
        return physician;
    }
    // setter for physician
    public void setPhysician(Doctor physician) {
        this.physician = physician;
    }
    //adding and removing emergency contacts
    public void addEmergencyContact(String contact) {
        if (!emergencyContacts.contains(contact)) {
            emergencyContacts.add(contact);
            System.out.println("Emergency contact added for patient: " + getName());
        } else {
            System.out.println("This contact already exists for patient: " + getName());
        }
    }
    public void removeEmergencyContact(String contact) {
        if (emergencyContacts.remove(contact)) {
            System.out.println("Emergency contact removed for patient: " + getName());
        } else {
            System.out.println("Contact not found for patient: " + getName());
        }
    }

    // no setters for VitalsDatabase and feedbacks because doesnt make sense to change vitasldatabase and feedbacks after the patient has been created





    //
    public void panicButton(String message) {
        PanicButton.pressPanicButton(message ,this);
    }

    // requesting a new appointment
    public void requestAppointment(Appointment appointment) {
        AppointmentManager.requestAppointment(appointment);
        System.out.println("Appointment requested for: " + getName());
    }


    // Add auto-save to methods that modify patient data:

    // Modify uploadVitalSign method
    public String uploadVitalSign(VitalSign vital) {
        vitalsDatabase.addVital(vital);
        System.out.println("Vital sign added for patient: " + getName());
        DataManager.savePatient(this); // Auto-save this patient
        DataManager.saveAllData(); // Auto-save all data

        return EmergencyAlert.checkVitalSigns(this, vital); // returns alert message or null
    }

    // Modify addFeedback method
    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
        System.out.println("Feedback added for patient: " + getName());
        DataManager.savePatient(this); // Auto-save
        DataManager.saveAllData(); // Auto-save all data
    }

    // Modify removeFeedback method
    public void removeFeedback(Feedback feedback) {
        feedbacks.remove(feedback);
        System.out.println("Feedback removed for patient: " + getName());
        DataManager.savePatient(this); // Auto-save
        DataManager.saveAllData(); // Auto-save all data
    }

    // Modify removeVital method
    public void removeVital(VitalSign vitalSign){
        vitalsDatabase.removeVital(vitalSign);
        DataManager.savePatient(this); // Auto-save
        DataManager.saveAllData(); // Auto-save all data
    }

    // returns list of feedback comments
    public ArrayList<String> viewPreviousFeedbacks() {
        ArrayList<String> feedbackComments = new ArrayList<>();
        for (Feedback f : feedbacks) {
            feedbackComments.add(f.getComments());
        }
        return feedbackComments;
    }

    // returns list of vitals
    public ArrayList<VitalSign> viewPreviousVitals() {
        return vitalsDatabase.getVitals();
    }

    public ArrayList<String> uploadVitalsFromCSV(String filePath) {
        ArrayList<String> alerts = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            if ((line = br.readLine()) != null) {
                // skip header
            }

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length != 5) {
                    System.out.println("Invalid data format: " + line);
                    continue;
                }

                double heartRate = Double.parseDouble(data[0].trim());
                double oxygenLevel = Double.parseDouble(data[1].trim());
                String bloodPressure = data[2].trim();
                double temperature = Double.parseDouble(data[3].trim());
                LocalDateTime dateTimeRecorded = LocalDateTime.parse(data[4].trim());

                VitalSign vital = new VitalSign(this.getId(), heartRate, oxygenLevel, bloodPressure, temperature, dateTimeRecorded);
                String alert = uploadVitalSign(vital); // this may return an alert
                if (alert != null) {
                    alerts.add(alert);
                }
            }
            System.out.println("Vitals uploaded successfully from CSV.");
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading CSV file: " + e.getMessage());
        }

        return alerts; // return list of critical alerts
    }


    public ArrayList<Appointment> viewAppointments() {
        ArrayList<Appointment> result = new ArrayList<>();
        for (Appointment appt : AppointmentManager.getAppointments()) {
            if (appt.getPatient().equals(this)) {
                result.add(appt);
            }
        }
        return result;
    }

    public boolean cancelAppointment(Appointment appt) {
        if (appt.getPatient().equals(this)) {
            return AppointmentManager.getAppointments().remove(appt);
        }
        return false;
    }

    public void requestVideoCall(VideoCall videocall) {
        AppointmentManager.requestVideoCall(videocall);
    }

    // viewing video calls
    public ArrayList<VideoCall> viewVideoCalls() {
        ArrayList<VideoCall> result = new ArrayList<>();
        for (VideoCall videoCall : AppointmentManager.getVideoCalls()) {
            if (videoCall.getPatient().equals(this)) {
                result.add(videoCall);
            }
        }
        return result;
    }

    // cancelling the video call
    public boolean cancelVideoCall(VideoCall videoCall) {
        if (videoCall.getPatient().equals(this)) {
            return AppointmentManager.getVideoCalls().remove(videoCall);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Patient other = (Patient) obj;
        return this.getId().equals(other.getId());
    }





    // no toString bcs user's can be used. no need to didplay vitals and feedbacks in this
}
