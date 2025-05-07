package com.rpms.HealthDataHandling;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class VitalsDatabase implements Serializable {
    // Static list to hold all vital signs
    private ArrayList<VitalSign> vitals = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    private transient Scene scene;
    private transient Stage stage;

    //getters and setters for scene and stage
    public Scene getScene() {
        return scene;
    }
    public void setScene(Scene scene) {
        this.scene = scene;
    }
    public Stage getStage() {
        return stage;
    }
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // getter for vitals

    public ArrayList<VitalSign> getVitals() {
        if (vitals == null) {
            vitals = new ArrayList<>();
        }
        return vitals;
    }


    // adding a new vital
    public void addVital(VitalSign vital) {
        vitals.add(vital);
        System.out.println("Vital sign added to database.");
    }
    // removing a vital sign
    public void removeVital(VitalSign vital) {
        vitals.remove(vital);
        System.out.println("Vital sign removed from database.");
    }

    public void generateVitalsGraph(Stage stage) {
        if (vitals.isEmpty()) {
            System.out.println("No vitals to display.");
            return;
        }

        // Common X-axis (DateTime)
        final CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("DateTime");

        // Y-axis for values
        final NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");

        // LineChart
        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Vital Signs Over Time");

        // Data series
        XYChart.Series<String, Number> heartRateSeries = new XYChart.Series<>();
        heartRateSeries.setName("Heart Rate (bpm)");

        XYChart.Series<String, Number> oxygenLevelSeries = new XYChart.Series<>();
        oxygenLevelSeries.setName("Oxygen Level (%)");

        XYChart.Series<String, Number> temperatureSeries = new XYChart.Series<>();
        temperatureSeries.setName("Temperature (°C)");

        // Formatter for timestamps
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM HH:mm");

        for (VitalSign vital : vitals) {
            String time = vital.getDateTimeRecorded().format(formatter);
            heartRateSeries.getData().add(new XYChart.Data<>(time, vital.getHeartRate()));
            oxygenLevelSeries.getData().add(new XYChart.Data<>(time, vital.getOxygenLevel()));
            temperatureSeries.getData().add(new XYChart.Data<>(time, vital.getTemperature()));
        }

        lineChart.getData().add(heartRateSeries);
        lineChart.getData().add(oxygenLevelSeries);
        lineChart.getData().add(temperatureSeries);

        // Show chart
        VBox vbox = new VBox(lineChart);
        Scene scene = new Scene(vbox, 800, 600);
        stage.setTitle("Patient Vital Signs Visualization");
        stage.setScene(scene);
        stage.show();
    }


    // if all the vital signs are to be displayed for a specific user ID
    public void displayVitals() {
        System.out.println("Vital Signs: ");
        for (VitalSign v : vitals) {
            System.out.println(v);
        }
    }
    // Add these methods to ensure proper serialization

    // This gets called after deserializing the object
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Initialize transient fields
        this.scene = null;
        this.stage = null;
    }




}
