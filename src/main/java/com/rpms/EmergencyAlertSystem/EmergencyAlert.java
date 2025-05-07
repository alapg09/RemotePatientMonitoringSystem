package com.rpms.EmergencyAlertSystem;

import com.rpms.HealthDataHandling.VitalSign;
import com.rpms.UserManagement.Patient;

/**
 * Handles detection and processing of critical vital signs in the system.
 * Contains static methods for evaluating vital signs and triggering alerts when necessary.
 */
public class EmergencyAlert {
    
    /**
     * Checks if a vital sign reading is abnormal and sends alerts if necessary.
     * This method evaluates the given vital sign against medical thresholds,
     * and if abnormal, sends notifications to the patient's physician and emergency contacts.
     * 
     * @param patient The patient whose vital sign is being checked
     * @param vital The vital sign to evaluate
     * @return Alert message if abnormal, null if normal
     */
    public static String checkVitalSigns(Patient patient, VitalSign vital) {
        if (isVitalSignAbnormal(vital)) {
            String alertMsg = "Emergency Alert: Abnormal vital signs detected for Patient "
                    + patient.getName() + " (ID: " + patient.getId() + ")";
            NotificationService.sendAlert(alertMsg, patient);
            return alertMsg;
        }
        return null;
    }

    /**
     * Determines if a vital sign is outside normal medical ranges.
     * Evaluates heart rate, oxygen level, blood pressure, and temperature
     * against established medical thresholds.
     * 
     * @param vital The vital sign to evaluate
     * @return true if any measurement is abnormal, false if all are normal
     */
    public static boolean isVitalSignAbnormal(VitalSign vital) {
        // Heart rate outside normal range (60-100 bpm)
        if (vital.getHeartRate() < 60 || vital.getHeartRate() > 100) return true;
        
        // Oxygen saturation below 90% (hypoxemia)
        if (vital.getOxygenLevel() < 90) return true;

        // Blood pressure outside normal range
        String[] bpParts = vital.getBloodPressure().split("/");
        int systolic = Integer.parseInt(bpParts[0]);
        int diastolic = Integer.parseInt(bpParts[1]);
        if (systolic < 90 || systolic > 140 || diastolic < 60 || diastolic > 90) return true;

        // Temperature outside normal range (36.1-37.2°C)
        if (vital.getTemperature() < 36.1 || vital.getTemperature() > 37.2) return true;

        return false; // all normal
    }
}
