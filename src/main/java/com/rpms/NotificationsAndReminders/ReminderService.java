package com.rpms.NotificationsAndReminders;

import com.rpms.AppointmentScheduling.*;
import com.rpms.DoctorPatientInteraction.*;
import com.rpms.UserManagement.*;
import com.rpms.ChatAndVideoConsultation.VideoCall;

// user will have the choice to have the object of this class to remind them of their appointments and medications
public class ReminderService {
    User user; // can be patient or doctor

    // different constructors
    public ReminderService(Patient patient) {
        this.user = patient;
    }
    public ReminderService(Doctor doctor) {
        this.user = doctor;
    }

    public User getUser() {
        return user;
    }

    public String getAppointmentReminders() {
        StringBuilder sb = new StringBuilder();
        for (Appointment appointment : AppointmentManager.getAppointments()) {
            if ((appointment.getPatient().equals(user) || appointment.getDoctor().equals(user)) 
            && appointment.getStatus().equals("Approved")) {
                if (appointment.getDateTime().isAfter(java.time.LocalDateTime.now())) {
                    sb.append(appointment).append("\n\n");
                }
            }
        }
        // sending email/sms alert to the patient
        new SMSNotification(this.getUser().getPhoneNumber(), sb.toString());
        new EmailNotification(this.getUser().getEmail(), "Medication Reminder", sb.toString());
        return sb.toString();
    }

    public String getMedicationReminders() {
        if (!(user instanceof Patient patient)) return "";

        StringBuilder sb = new StringBuilder();
        for (Feedback fb : patient.getFeedbacks()) {
            for (Prescription prescription : fb.getPrescriptions()) {
                sb.append(prescription).append("\n");
            }
        }
        // sending email/sms alert to the patient
        new SMSNotification(this.getUser().getPhoneNumber(), sb.toString());
        new EmailNotification(this.getUser().getEmail(), "Medication Reminder", sb.toString());
        return sb.toString();
    }
    
    /**
     * Gets upcoming approved video calls for the user
     * @return String containing all approved video calls information
     */
    public String getApprovedVideoCalls() {
        StringBuilder sb = new StringBuilder();
        for (VideoCall videoCall : AppointmentManager.getVideoCalls()) {
            if ((videoCall.getPatient().equals(user) || videoCall.getDoctor().equals(user)) 
                    && videoCall.getStatus().equals("Approved")) {
                // Only include upcoming video calls
                if (videoCall.getStartTime().isAfter(java.time.LocalDateTime.now())) {
                    sb.append(videoCall).append("\n\n");
                }
            }
        }
        
        // If there are approved video calls, send notifications
        if (sb.length() > 0) {
            // sending email/sms alert to the user
            new SMSNotification(this.getUser().getPhoneNumber(), sb.toString());
            new EmailNotification(this.getUser().getEmail(), "Upcoming Video Consultations", sb.toString());
        }
        
        return sb.toString();
    }
}
