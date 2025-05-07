package com.rpms.UserManagement;

// required imports
import com.rpms.AppointmentScheduling.Appointment;
import com.rpms.AppointmentScheduling.AppointmentManager;
import com.rpms.ChatAndVideoConsultation.VideoCall;
import com.rpms.DoctorPatientInteraction.Feedback;
import com.rpms.EmergencyAlertSystem.EmergencyAlert;
import com.rpms.HealthDataHandling.VitalSign;
import com.rpms.utilities.DataManager;

import java.util.ArrayList;

public class Doctor extends User {
    // arraylists of patients that are assigned to a Doctor
    private ArrayList<Patient> patients;

    private static final long serialVersionUID = 1L;

    // constructor
    public Doctor(String id, String name,String phoneNumber, String email, String username, String password) {
        super(id, name, phoneNumber, email, username, password);
        this.patients = new ArrayList<>();  // iniitializing the new arraylist of patients for each doctor
    }

    @Override
    public String getRole() {
        return "Doctor";
    }


    // gettern for patients
    public ArrayList<Patient> getPatients() {
        return patients;
    }

    // no setter for patients bcs doesn't make sense


    public void addPatient(Patient patient) {
        patients.add(patient);
        System.out.println("Patient " + patient.getName() + " added to Dr. " + getName() + "'s list.");
        DataManager.saveDoctor(this); // Auto-save
        DataManager.saveAllData(); // Auto-save all data
    }
    // giving feedback to a patient
    public void provideFeedback(Patient patient, Feedback feedback) {
        patient.addFeedback(feedback);
    }

    // viewing appointments for doctors
    public ArrayList<Appointment> getAppointments() {
        ArrayList<Appointment> appointments = new ArrayList<>();
        for (Appointment a : AppointmentManager.getAppointments()) {
            if (a.getDoctor().equals(this)) {
                appointments.add(a);
            }
        }
        return appointments;
    }

    // viewing patients
    public void viewPatients() {
        System.out.println("Patients for Dr. " + getName() + ":");
        for (Patient p : patients) {
            System.out.println(p.getName());
        }
    }

    // viewing patient feedbacks
    public void viewPatientFeedbacks(Patient patient) {
        System.out.println("Feedbacks for " + patient.getName() + ":");
        for (Feedback f : patient.getFeedbacks()) {
            System.out.println(f);
        }
    }
    // getter for pending appointments
    public ArrayList<Appointment> getPendingAndApprovedAppointments() {
        ArrayList<Appointment> pendingAppointments = new ArrayList<>();

        for (Appointment a : AppointmentManager.getAppointments()) {
            if (a.getDoctor().equals(this) && (a.getStatus().equals("Pending") || a.getStatus().equals("Approved"))) {
                pendingAppointments.add(a);
            }
        }
        return pendingAppointments;
    }

    // viewing patient vitals
    public void viewPatientVitals(Patient patient) {
        System.out.println("Vitals for " + patient.getName() + ":");
        System.out.println(patient.getVitals());
    }

    // approving appointments
    public void approveAppointment(Appointment appointment) {
        AppointmentManager.approveAppointment(appointment);
        System.out.println("Appointment approved for: " + appointment.getPatient().getName());
    }

    // cancelling appointments
    public void cancelAppointment(Appointment appointment) {
        AppointmentManager.cancelAppointment(appointment);
        System.out.println("Appointment cancelled for: " + appointment.getPatient().getName());
    }

    // approving video calls
    public void approveVideoCall(VideoCall videoCall) {
        AppointmentManager.approveVideoCall(videoCall);
        System.out.println("Video call approved for: " + videoCall.getPatient().getName() + " with Dr. " + getName() + " at " + videoCall.getStartTime() + " to " + videoCall.getEndTime());
    }
    // getter for video calls
    public ArrayList<VideoCall> getVideoCalls() {
        ArrayList<VideoCall> videocalls = new ArrayList<>();
        for(VideoCall v : AppointmentManager.getVideoCalls()){
            if(v.getDoctor().equals(this)){
                videocalls.add(v);
            }
        }
        return videocalls;
    }

    // cancelling video calls
    public void cancelVideoCall(VideoCall videoCall) {
        AppointmentManager.cancelVideoCall(videoCall);
        System.out.println("Video call cancelled for: " + videoCall.getPatient().getName() + " with Dr. " + getName() + " at " + videoCall.getStartTime() + " to " + videoCall.getEndTime());
    }


    public String patientCriticalVitalDetection(){
        StringBuilder criticalPatients = new StringBuilder();
        for(Patient p : patients){
            for(VitalSign v : p.getVitals().getVitals()){
                if(EmergencyAlert.isVitalSignAbnormal(v)){
                    criticalPatients.append("Patient: ").append(p.getName()).append(" has critical vitals: \n").append(v).append("\n");
                }
            }
        }
        return criticalPatients.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Doctor other = (Doctor) obj;
        boolean result = this.getId().equals(other.getId());
        return result;
    }
}
