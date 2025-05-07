package com.rpms.HealthDataHandling;


import com.rpms.utilities.DateUtil;

import java.io.Serializable;
import java.time.LocalDateTime;

public class VitalSign implements Serializable {
    // attributes for vital signs including date for keeping track of when they were recorded
    private final String patientID;
    private double heartRate;
    private double oxygenLevel;
    private String bloodPressure;
    private double temperature;
    private LocalDateTime dateTimeRecorded;

    private static final long serialVersionUID = 1L;


    // constructor to initialize the vital sign attributes
    public VitalSign(String patientID, double heartRate, double oxygenLevel, String bloodPressure, double temperature, LocalDateTime dateTimeRecorded) {
        this.patientID = patientID;
        setHeartRate(heartRate);
        setOxygenLevel(oxygenLevel);
        setBloodPressure(bloodPressure);
        setTemperature(temperature);
        setDateRecorded(dateTimeRecorded);
    }

    // getters
    public String getUserId() { return patientID; }
    public double getHeartRate() { return heartRate;}
    public double getOxygenLevel() { return oxygenLevel;}
    public String getBloodPressure() { return bloodPressure;}
    public double getTemperature() { return temperature;}
    public LocalDateTime getDateTimeRecorded() { return dateTimeRecorded;}

    // setters in case modification is required
    public void setHeartRate(double heartRate) {
        if (heartRate < 30 || heartRate > 200) {
            throw new IllegalArgumentException("Invalid heart rate. It must be between 30 and 200 bpm.");
        }
        this.heartRate = heartRate;
    }

    public void setOxygenLevel(double oxygenLevel) {
        if (oxygenLevel < 0 || oxygenLevel > 100) {
            throw new IllegalArgumentException("Invalid oxygen level. It must be between 0 and 100 percent.");
        }
        this.oxygenLevel = oxygenLevel;
    }

    public void setBloodPressure(String bloodPressure) {
        String[] bpParts = bloodPressure.split("/");
        if (bpParts.length != 2) {
            throw new IllegalArgumentException("Invalid blood pressure format. It must be in the form 'systolic/diastolic'.");
        }

        try {
            int systolic = Integer.parseInt(bpParts[0]);
            int diastolic = Integer.parseInt(bpParts[1]);

            if (systolic < 60 || systolic > 250 || diastolic < 30 || diastolic > 150) {
                throw new IllegalArgumentException("Invalid blood pressure. Systolic must be between 60 and 250, and diastolic must be between 30 and 150.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid blood pressure values. Please provide numeric values.");
        }

        this.bloodPressure = bloodPressure;
    }

    public void setTemperature(double temperature) {
        if (temperature < 35.0 || temperature > 42.0) {
            throw new IllegalArgumentException("Invalid temperature. It must be between 35.0 and 42.0 degrees Celsius.");
        }
        this.temperature = temperature;
    }

    public void setDateRecorded(LocalDateTime dateTimeRecorded) {
        if (dateTimeRecorded == null) {
            throw new IllegalArgumentException("Date and time recorded cannot be null.");
        }
        this.dateTimeRecorded = dateTimeRecorded;
    }



    // overriden toString method to display the vital sign details
    @Override
    public String toString() {
        return "HR: " + heartRate +
                "\nO2: " + oxygenLevel +
                "%\nTemp: " + temperature + "°C" +
                "\nDate & Time: " + DateUtil.format(dateTimeRecorded) + "/n/n";
    }
}
