package com.rpms.UserManagement;

// Required imports
import com.rpms.AppointmentScheduling.Appointment;
import com.rpms.AppointmentScheduling.AppointmentManager;
import com.rpms.ChatAndVideoConsultation.VideoCall;
import com.rpms.EmergencyAlertSystem.EmergencyAlert;
import com.rpms.HealthDataHandling.VitalSign;
import com.rpms.utilities.DataManager;

import java.util.ArrayList;

/**
 * Doctor class representing a medical professional in the healthcare system.
 * Extends the User class with doctor-specific functionality.
 */
public class Doctor extends User {
    /** List of patients assigned to this doctor */
    private ArrayList<Patient> patients;

    /** Serialization version identifier */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor to initialize a new Doctor with all required fields
     * 
     * @param id Unique identifier for the doctor
     * @param name Full name of the doctor
     * @param phoneNumber Contact phone number
     * @param email Email address
     * @param username Username for login
     * @param password Password for authentication
     */
    public Doctor(String id, String name, String phoneNumber, String email, String username, String password) {
        super(id, name, phoneNumber, email, username, password);
        this.patients = new ArrayList<>();  // Initializing the new ArrayList of patients for each doctor
    }

    /**
     * Returns the role of this user
     * @return "Doctor" as the role
     */
    @Override
    public String getRole() {
        return "Doctor";
    }

    // ===== Getters and Setters =====
    
    /**
     * Gets the list of patients assigned to this doctor
     * @return ArrayList of patients
     */
    public ArrayList<Patient> getPatients() {
        return patients;
    }
    // No setter for patients as it doesn't make sense to completely replace the list

    // ===== Patient Management Methods =====
    
    /**
     * Adds a patient to this doctor's list
     * @param patient The patient to add
     */
    public void addPatient(Patient patient) {
        patients.add(patient);
        System.out.println("Patient " + patient.getName() + " added to Dr. " + getName() + "'s list.");
        DataManager.saveDoctor(this); // Auto-save
        DataManager.saveAllData(); // Auto-save all data
    }


    // ===== Vital Signs Methods =====

    
    /**
     * Detects patients with critical vital signs
     * @return A formatted string with information about patients with critical vitals
     */
    public String patientCriticalVitalDetection() {
        StringBuilder criticalPatients = new StringBuilder();
        for (Patient p : patients) {
            for (VitalSign v : p.getVitals().getVitals()) {
                if (EmergencyAlert.isVitalSignAbnormal(v)) {
                    criticalPatients.append("Patient: ").append(p.getName())
                                   .append(" has critical vitals: \n")
                                   .append(v).append("\n");
                }
            }
        }
        return criticalPatients.toString();
    }

    // ===== Appointment Methods =====
    
    /**
     * Gets all appointments for this doctor
     * @return ArrayList of appointments
     */
    public ArrayList<Appointment> getAppointments() {
        ArrayList<Appointment> appointments = new ArrayList<>();
        for (Appointment a : AppointmentManager.getAppointments()) {
            if (a.getDoctor().equals(this)) {
                appointments.add(a);
            }
        }
        return appointments;
    }
    
    /**
     * Gets all pending and approved appointments for this doctor
     * @return ArrayList of pending and approved appointments
     */
    public ArrayList<Appointment> getPendingAndApprovedAppointments() {
        ArrayList<Appointment> pendingAppointments = new ArrayList<>();
        for (Appointment a : AppointmentManager.getAppointments()) {
            if (a.getDoctor().equals(this) && 
               (a.getStatus().equals("Pending") || a.getStatus().equals("Approved"))) {
                pendingAppointments.add(a);
            }
        }
        return pendingAppointments;
    }
    
    /**
     * Approves a specific appointment
     * @param appointment The appointment to approve
     */
    public void approveAppointment(Appointment appointment) {
        AppointmentManager.approveAppointment(appointment);
        System.out.println("Appointment approved for: " + appointment.getPatient().getName());
    }
    
    /**
     * Cancels a specific appointment
     * @param appointment The appointment to cancel
     */
    public void cancelAppointment(Appointment appointment) {
        AppointmentManager.cancelAppointment(appointment);
        System.out.println("Appointment cancelled for: " + appointment.getPatient().getName());
    }

    // ===== Video Call Methods =====
    
    /**
     * Gets all video calls for this doctor
     * @return ArrayList of video calls
     */
    public ArrayList<VideoCall> getVideoCalls() {
        ArrayList<VideoCall> videocalls = new ArrayList<>();
        for (VideoCall v : AppointmentManager.getVideoCalls()) {
            if (v.getDoctor().equals(this)) {
                videocalls.add(v);
            }
        }
        return videocalls;
    }
    
    /**
     * Approves a specific video call
     * @param videoCall The video call to approve
     */
    public void approveVideoCall(VideoCall videoCall) {
        AppointmentManager.approveVideoCall(videoCall);
        System.out.println("Video call approved for: " + videoCall.getPatient().getName() + 
                          " with Dr. " + getName() + " at " + videoCall.getStartTime() + 
                          " to " + videoCall.getEndTime());
    }
    
    /**
     * Cancels a specific video call
     * @param videoCall The video call to cancel
     */
    public void cancelVideoCall(VideoCall videoCall) {
        AppointmentManager.cancelVideoCall(videoCall);
        System.out.println("Video call cancelled for: " + videoCall.getPatient().getName() + 
                          " with Dr. " + getName() + " at " + videoCall.getStartTime() + 
                          " to " + videoCall.getEndTime());
    }

    // ===== Object Overrides =====
    
    /**
     * Checks if this doctor equals another doctor based on ID
     * @param obj The object to compare with
     * @return true if IDs match, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Doctor other = (Doctor) obj;
        return this.getId().equals(other.getId());
    }
}
