package com.rpms.EmergencyAlertSystem;

import com.rpms.HealthDataHandling.VitalSign;
import com.rpms.UserManagement.Patient;

public class EmergencyAlert {
    // static method so that it can be called without creating an object of the class
    public static String checkVitalSigns(Patient patient, VitalSign vital) {
        if (isVitalSignAbnormal(vital)) {
            String alertMsg = "⚠️ Emergency Alert: Abnormal vital signs detected for Patient "
                    + patient.getName() + " (ID: " + patient.getId() + ")";
            NotificationService.sendAlert(alertMsg, patient);
            return alertMsg;
        }
        return null;
    }


    // inside EmergencyAlert.java

    // returns true if the vital sign is abnormal
    public static boolean isVitalSignAbnormal(VitalSign vital) {
        // same checks as before
        if (vital.getHeartRate() < 60 || vital.getHeartRate() > 100) return true;
        if (vital.getOxygenLevel() < 90) return true;

        String[] bpParts = vital.getBloodPressure().split("/");
        int systolic = Integer.parseInt(bpParts[0]);
        int diastolic = Integer.parseInt(bpParts[1]);
        if (systolic < 90 || systolic > 140 || diastolic < 60 || diastolic > 90) return true;

        if (vital.getTemperature() < 36.1 || vital.getTemperature() > 37.2) return true;

        return false; // all normal
    }


}
