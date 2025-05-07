package com.rpms.EmergencyAlertSystem;

import com.rpms.UserManagement.Patient;

// This class represents the Panic Button functionality in the emergency alert system.
// patients have a method to press the panic button.

public class PanicButton {
    // static method to simulate pressing the panic button
    // this method will be called when the patient presses the panic button
    public static void pressPanicButton(String message, Patient patient) {
        // Trigger emergency alert
        NotificationService.sendAlert(message, patient);
    }
}
