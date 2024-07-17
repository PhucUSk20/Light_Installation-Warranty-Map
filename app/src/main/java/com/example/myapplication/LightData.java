package com.example.myapplication;

import java.util.UUID;

public class LightData {
    private String error;
    private String location;
    private int Control;
    private String sensorEnv;
    private String sensorLig;
    private String value;

    private String assignedTo;
    private String lightname;

    private String imageUrl;

    // Default constructor required for calls to DataSnapshot.getValue(LightData.class)
    public LightData() {
    }


    public LightData(String lightname, String error, String location, int Control, String sensorEnv, String sensorLig, String value, String assignedTo) {
        this.lightname = lightname;
        this.error = error;
        this.location = location;
        this.Control  = Control;
        this.sensorEnv = sensorEnv;
        this.sensorLig = sensorLig;
        this.value = value;
        this.assignedTo = assignedTo;
    }

    public String getLightName() {
        return lightname;
    }

    public void setLightName(String lightname) {
        this.lightname = lightname;
    }

    // Getters and setters for all fields
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getMode() {
        return Control;
    }

    public void setMode(int Control) {
        this.Control = Control;
    }

    public String getSensorEnv() {
        return sensorEnv;
    }

    public void setSensorEnv(String sensorEnv) {
        this.sensorEnv = sensorEnv;
    }

    public String getSensorLig() {
        return sensorLig;
    }

    public void setSensorLig(String sensorLig) {
        this.sensorLig = sensorLig;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    @Override
    public String toString() {
        return lightname;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
