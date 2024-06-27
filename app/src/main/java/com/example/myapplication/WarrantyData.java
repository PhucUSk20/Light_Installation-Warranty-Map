package com.example.myapplication;

public class WarrantyData {
    private String lightid;
    private String status;
    private String timestamp;

    public WarrantyData() {
        // Required empty public constructor
    }

    public WarrantyData(String lightId, String status, String timestamp) {
        this.lightid = lightId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getLightId() {
        return lightid;
    }

    public void setLightId(String lightId) {
        this.lightid = lightId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
