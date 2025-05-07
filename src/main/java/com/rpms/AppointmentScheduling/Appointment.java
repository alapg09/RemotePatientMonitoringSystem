package com.rpms.AppointmentScheduling;

import com.rpms.UserManagement.Doctor;
import com.rpms.UserManagement.Patient;
import com.rpms.UserManagement.Administrator;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.rpms.utilities.DateUtil;

public class Appointment implements Serializable {
    // data fields
    private LocalDateTime datetime;
    private String status;
    private Doctor doctor;
    private Patient patient;

    private static final long serialVersionUID = 1L;


    // constructor
    public Appointment(LocalDateTime datetime, Doctor doctor, Patient patient) {
        this.datetime = datetime;
        // using setters to ensure validation
        setDoctor(doctor);
        setPatient(patient);
        setStatus("Pending");
    }



    // getters
    public LocalDateTime getDateTime() { return datetime; }
    public String getStatus() { return status; }
    public Doctor getDoctor() { return doctor; }
    public Patient getPatient() { return patient; }

    // setters
    public void setDate(LocalDateTime datetime) {
        this.datetime = datetime;
    }
    public void setStatus(String status) {
        // vallidation for states of status
        if (status.equals("Pending") || status.equals("Approved") || status.equals("Cancelled")) {
            this.status = status;
        } else {
            System.out.println("Invalid status. Status must be either 'Pending', 'Approved', or 'Cancelled'.");
        }
    }
    public void setDoctor(Doctor doctor) {
        // checking if the doctor exists in the hospital
        for (Doctor d : Administrator.getDoctors()) {
            if (d.equals(doctor)) {
                this.doctor = doctor;
            }
        }

    }
    public void setPatient(Patient patient) {
        // checking if the patient exists in the hospital
        for (Patient p : Administrator.getPatients()) {
            if (p.equals(patient)) {
                this.patient = patient;
            }
        }
    }

    @Override
    public String toString() {
        return "\n\nAppointment" +
                "\nDate & Time : " + DateUtil.format(datetime) +
                "\nStatus : '" + status + '\'' +
                "\nDoctor : " + doctor.getName() +
                "\nPatient : " + patient.getName();
    }
}
