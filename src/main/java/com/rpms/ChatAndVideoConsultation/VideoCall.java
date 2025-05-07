package com.rpms.ChatAndVideoConsultation;
import com.rpms.UserManagement.Doctor;
import com.rpms.UserManagement.Patient;
import com.rpms.utilities.DateUtil;

import java.io.Serializable;
import java.time.LocalDateTime;

public class VideoCall implements Serializable {
    // data fields
    private Doctor doctor;
    private Patient patient;
    private String meetingLink;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    private static final long serialVersionUID = 1L;


    // constructor taking meeting link as input
    public VideoCall(Doctor doctor, Patient patient, LocalDateTime startTime, LocalDateTime endTime) {
        this.doctor = doctor;
        this.patient = patient;
        this.meetingLink = "notprovided";
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "Pending";
    }

    // getters and setters
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public String getLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        if (status.equals("Pending") || status.equals("Approved") || status.equals("Cancelled")) {
            this.status = status;
        } else {
            System.out.println("Invalid status. Status must be either 'Pending', 'In Progress', or 'Completed'.");
        }
    }



    public String startCall() {
        return "Video Call between " + doctor.getName() + " and " + patient.getName() + "\nJoin here: " + meetingLink +
                "\nStart Time: " + startTime + "\nEnd Time: " + endTime;
    }

    @Override
    public String toString() {
        return "VideoCall" +
                "\nDoctor=" + doctor.getName() +
                "\nPatient=" + patient.getName() +
                "\nMeetingLink='" + meetingLink + '\'' +
                "\nstartTime=" + DateUtil.format(startTime) +
                "\nendTime=" + DateUtil.format(endTime) +
                "\nstatus='" + status + '\'';
    }

    public void setLink(String link) {
        this.meetingLink = link;
    }
}
