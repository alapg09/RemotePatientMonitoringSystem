package com.rpms.AppointmentScheduling;

import java.util.ArrayList;
import com.rpms.ChatAndVideoConsultation.VideoCall;
import com.rpms.utilities.*;

// everything is static because the methods of appointment manager do not need an object
public class AppointmentManager {
    // static arraylist to hold all appointments
    private static ArrayList<Appointment> appointments = new ArrayList<>();
    private static ArrayList<VideoCall> videoCalls = new ArrayList<>();

    // getter for appointments
    public static ArrayList<Appointment> getAppointments() {
        return appointments;
    }
    // getter for video calls
    public static ArrayList<VideoCall> getVideoCalls() {
        return videoCalls;
    }


    // Add at the end of existing methods:

    // Modify requestAppointment method
    public static void requestAppointment(Appointment appointment) {
        appointments.add(appointment);
        System.out.println("Appointment added to queue. Waiting to be approved.");
        DataManager.saveAllData(); // Auto-save
    }

    // Modify approveAppointment method
    public static void approveAppointment(Appointment appointment) {
        appointment.setStatus("Approved");
        System.out.println("Appointment approved: " + appointment.getDateTime());
        // checking if patient already exists
        if(!appointment.getDoctor().getPatients().contains(appointment.getPatient())){
            appointment.getDoctor().addPatient(appointment.getPatient());
        }
        DataManager.saveAllData(); // Auto-save
    }

    // Modify cancelAppointment method
    public static void cancelAppointment(Appointment appointment) {
        appointment.setStatus("Cancelled");
        System.out.println("Appointment cancelled: " + appointment.getDateTime());
        DataManager.saveAllData(); // Auto-save
    }

    // Modify requestVideoCall method
    public static void requestVideoCall(VideoCall videocall) {
        videoCalls.add(videocall);
        System.out.println("Video call requested for appointment: " + videocall.getStartTime() + " to " + videocall.getEndTime());
        DataManager.saveAllData(); // Auto-save
    }

    // Modify cancelVideoCall method
    public static void cancelVideoCall(VideoCall videocall) {
        videoCalls.remove(videocall);
        videocall.setStatus("Cancelled");
        System.out.println("Video call cancelled for appointment: " + videocall.getStartTime() + " to " + videocall.getEndTime());
        DataManager.saveAllData(); // Auto-save
    }

    // Modify approveVideoCall method
    public static void approveVideoCall(VideoCall videocall) {
        videocall.setStatus("Approved");
        System.out.println("Video call approved: " + videocall.getStartTime());
        // add the patient to doctor when appointment is approved
        if(!videocall.getDoctor().getPatients().contains(videocall.getPatient())){
            videocall.getDoctor().addPatient(videocall.getPatient());
        }
        DataManager.saveAllData(); // Auto-save
    }

    // no toStirng method is needed here because the appointment manager is not an object that needs to be printed
}
