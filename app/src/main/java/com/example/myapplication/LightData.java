package com.example.myapplication;

import java.util.UUID;

public class LightData {
    private int error;
    private String location;
    private int mode;
    private int sensorEnv;
    private int sensorLig;
    private int value;

    private String lightname;

    // Default constructor required for calls to DataSnapshot.getValue(LightData.class)
    public LightData() {
    }


    public LightData(String lightname, int error, String location, int mode, int sensorEnv, int sensorLig, int value) {
        this.lightname = lightname;
        this.error = error;
        this.location = location;
        this.mode = mode;
        this.sensorEnv = sensorEnv;
        this.sensorLig = sensorLig;
        this.value = value;
    }

    public String getLightName() {
        return lightname;
    }

    public void setLightName(String lightname) {
        this.lightname = lightname;
    }

    // Getters and setters for all fields
    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getSensorEnv() {
        return sensorEnv;
    }

    public void setSensorEnv(int sensorEnv) {
        this.sensorEnv = sensorEnv;
    }

    public int getSensorLig() {
        return sensorLig;
    }

    public void setSensorLig(int sensorLig) {
        this.sensorLig = sensorLig;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
    @Override
    public String toString() {
        return lightname; // Return the lightName when toString() is called
    }
}
