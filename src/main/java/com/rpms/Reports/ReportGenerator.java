package com.rpms.Reports;

import com.rpms.DoctorPatientInteraction.Feedback;
import com.rpms.DoctorPatientInteraction.Prescription;
import com.rpms.HealthDataHandling.VitalSign;
import com.rpms.UserManagement.Patient;
import com.rpms.UserManagement.Doctor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;

public class ReportGenerator {

    public static void generatePatientReport(Patient patient, File file) {
        try (FileWriter writer = new FileWriter(file)) {

            // Header
            writer.write("Patient Report - " + patient.getName() + " (" + patient.getId() + ")\n");
            writer.write("--------------------------------------------------\n");

            // Doctor info
            Doctor doctor = patient.getPhysician();
            if (doctor != null) {
                writer.write("Assigned Doctor: " + doctor.getName() + "\n");
                writer.write("Contact: " + doctor.getEmail() + "\n\n");
            }

            // Vitals
            writer.write("Vitals History:\n");
            ArrayList<VitalSign> vitals = patient.getVitals().getVitals();
            for (VitalSign v : vitals) {
                writer.write(" - [" + v.getDateTimeRecorded() + "] HR: " + v.getHeartRate() +
                        " bpm, Temp: " + v.getTemperature() + "°C, BP: " + v.getBloodPressure() +
                        " mmHg, O2: " + v.getOxygenLevel() + "%\n");
            }

            writer.write("\nFeedback:\n");
            ArrayList<Feedback> feedbacks = patient.getFeedbacks();
            for (Feedback f : feedbacks) {
                writer.write(" - [" + f.getDate() + "] " + f.getComments() + "\n");
                ArrayList<Prescription> prescriptions = f.getPrescriptions();
                if (prescriptions != null && !prescriptions.isEmpty()) {
                    writer.write("   Prescriptions:\n");
                    for (Prescription p : prescriptions) {
                        writer.write("     • " + p.toString() + "\n");
                    }
                }
            }

            System.out.println("✅ Report generated: " + file.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("❌ Failed to generate report: " + e.getMessage());
        }
    }

}
