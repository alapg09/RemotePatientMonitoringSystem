package com.rpms.NotificationsAndReminders;

import com.rpms.AppointmentScheduling.*;
import com.rpms.DoctorPatientInteraction.*;
import com.rpms.UserManagement.*;

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
            if (appointment.getPatient().equals(user) || appointment.getDoctor().equals(user)) {
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
}
