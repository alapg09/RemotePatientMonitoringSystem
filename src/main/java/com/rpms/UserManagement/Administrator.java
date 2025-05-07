package com.rpms.UserManagement;

import java.util.ArrayList;

import com.rpms.utilities.DataManager;

public class Administrator extends User {
    // lists of patients and doctors in the system
    // they are statics because we want to have a single list of doctors, patients and administrators for the whole system
    // so that we can access them from anywhere in the system
    private static ArrayList<Doctor> doctors = new ArrayList<>();
    private static ArrayList<Patient> patients = new ArrayList<>();
    private static ArrayList<Administrator> admins = new ArrayList<>();
    private static final long serialVersionUID = 1L;

    // list of system logs
    private static ArrayList<String> systemLogs = new ArrayList<>();

    // constructor
    public Administrator(String id, String name, String phoneNumber, String email, String username,  String password) {
        super(id, name, phoneNumber, email, username, password);
    }

    @Override
    public String getRole() {
        return "Administrator";
    }

    // getters
    public static ArrayList<Doctor> getDoctors() { return doctors; }
    public static ArrayList<Patient> getPatients() { return patients; }
    public static ArrayList<Administrator> getAdministrators() { return admins; }
    // for login purposes
    public static ArrayList<User> getAllUsers() {
        ArrayList<User> allUsers = new ArrayList<>();
        allUsers.addAll(doctors);
        allUsers.addAll(patients);
        allUsers.addAll(admins);
        return allUsers;
    }
    public static ArrayList<String> getSystemLogs() { return systemLogs; }

    // adding new log
    public static void addSystemLog(String log) {
        systemLogs.add(log);
    }
    // clearing all logs
    public static void clearSystemLogs() {
        systemLogs.clear();
        System.out.println("System logs cleared.");
        // adding the log to the system logs
        systemLogs.add("System logs cleared.");
    }
    // viewing all logs
    public static void viewSystemLogs() {
        System.out.println("System Logs:");
        for (String log : systemLogs) {
            System.out.println(log);
        }
    }
    // Add auto-save to registration methods:

    // Modify registerDoctor method
    public static void registerDoctor(Doctor doctor) {
        doctors.add(doctor);
        // adding the doctor to the system logs
        systemLogs.add("Doctor " + doctor.getName() + " registered.");
        System.out.println("Doctor " + doctor.getName() + " registered.");
        DataManager.saveAllData(); // Auto-save
    }

    // Modify registerPatient method
    public static void registerPatient(Patient patient) {
        patients.add(patient);
        // adding the patient to the system logs
        systemLogs.add("Patient " + patient.getName() + " registered.");
        System.out.println("Patient " + patient.getName() + " registered.");
        DataManager.saveAllData(); // Auto-save
    }

    // Modify removeDoctor method
    public static void removeDoctor(Doctor doctor) {
        if (doctors.remove(doctor)) {
            System.out.println("Doctor " + doctor.getName() + " removed from the system.");
            // adding the doctor to the system logs
            systemLogs.add("Doctor " + doctor.getName() + " removed from the system.");
            DataManager.saveAllData(); // Auto-save
        } else {
            System.out.println("Doctor not found.");
        }
    }

    // Modify removePatient method
    public static void removePatient(Patient patient) {
        if (patients.remove(patient)) {
            System.out.println("Patient " + patient.getName() + " removed from the system.");
            // adding the patient to the system logs
            systemLogs.add("Patient " + patient.getName() + " removed from the system.");
            DataManager.saveAllData(); // Auto-save
        } else {
            System.out.println("Patient not found.");
        }
    }

    // Modify registerAdministrator method
    public static void registerAdministrator(Administrator admin) {
        admins.add(admin);
        System.out.println("Admin " + admin.getName() + " added to the system.");
        // adding the admin to the system logs
        systemLogs.add("Admin " + admin.getName() + " added to the system.");
        DataManager.saveAllData(); // Auto-save
    }

    // Modify removeAdministrator method
    public static void removeAdministrator(Administrator admin) {
        if (admins.remove(admin)) {
            System.out.println("Admin " + admin.getName() + " removed from the system.");
            // adding the admin to the system logs
            systemLogs.add("Admin " + admin.getName() + " removed from the system.");
            DataManager.saveAllData(); // Auto-save
        } else {
            System.out.println("Admin not found.");
        }
    }



}
