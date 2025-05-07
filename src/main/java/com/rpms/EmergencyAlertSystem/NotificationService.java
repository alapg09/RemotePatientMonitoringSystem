package com.rpms.EmergencyAlertSystem;

import com.rpms.NotificationsAndReminders.EmailNotification;
import com.rpms.NotificationsAndReminders.SMSNotification;
import com.rpms.UserManagement.Patient;

public class NotificationService {

    // sms alert
    public static void sendSMS(String message, Patient patient) {
        // creating new SMS object
        SMSNotification sms = new SMSNotification(patient.getPhysician().getPhoneNumber(), message);
        // sending it
        sms.sendNotification();

        // doing the same for all emergency contacts
        for(String contact : patient.getEmergencyContacts()) {
            SMSNotification emergencySms = new SMSNotification(contact, message);
            emergencySms.sendNotification();
        }
    }

    // email alert
    public static void sendEmail(String subject, String message, Patient patient) {
        // creating new Email object
        EmailNotification email = new EmailNotification(patient.getPhysician().getEmail(), subject, message);
        // sending it
        email.sendNotification();
        // doing the same for all emergency contacts
        for(String contact : patient.getEmergencyContacts()) {
            EmailNotification emergencyEmail = new EmailNotification(contact, subject, message);
            emergencyEmail.sendNotification();
        }
    }

    // both email and sms
    public static void sendAlert(String message, Patient patient) {
        // sending sms and email to the physician and emergency contacts
        sendSMS(message, patient);
        sendEmail("Emergency Alert", message, patient);
    }
}
