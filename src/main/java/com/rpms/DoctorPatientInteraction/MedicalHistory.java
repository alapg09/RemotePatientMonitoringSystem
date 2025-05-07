package com.rpms.DoctorPatientInteraction;

import java.util.ArrayList;
import com.rpms.UserManagement.Doctor;
import com.rpms.UserManagement.Patient;


public class MedicalHistory {
    // data fields
    private ArrayList<Feedback> pastConsulations;
    private Patient patient;
    private Doctor doctor;

    // constructors
    public MedicalHistory(Patient patient, Doctor doctor) {
        this.patient = patient;
        this.doctor = doctor;
        this.pastConsulations = new ArrayList<>();
    }

    // Getters and Setters
    public ArrayList<Feedback> getPastConsulations() { return pastConsulations; }
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }


    // setters
    public void setPatient(Patient patient) { this.patient = patient; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    // no setter for past consultations

    // adding new patient
    public void addPrescription(Feedback feedback) {
        pastConsulations.add(feedback);
        System.out.println("Prescription added to history.");
    }

    // removing a prescription
    public void removePrescription(Feedback feedback) {
        if (pastConsulations.remove(feedback)) {
            System.out.println("Prescription removed from history.");
        } else {
            System.out.println("Prescription not found in history.");
        }
    }

    // no toString for this class
}
