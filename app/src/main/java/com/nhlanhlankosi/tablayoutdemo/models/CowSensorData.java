package com.nhlanhlankosi.tablayoutdemo.models;

import java.util.ArrayList;

public class CowSensorData {

    private String cowName;
    private String cowId;
    private ArrayList<CowLocation> locations;
    private ArrayList<Double> temperatures;
    private ArrayList<Long> heartRates;

    public CowSensorData() {
    }

    public CowSensorData(String cowId, String cowName, ArrayList<CowLocation> locations, ArrayList<Double> temperatures, ArrayList<Long> heartRates) {
        this.cowId = cowId;
        this.cowName = cowName;
        this.locations = locations;
        this.temperatures = temperatures;
        this.heartRates = heartRates;
    }

    public String getCowId() {
        return cowId;
    }

    public void setCowId(String cowId) {
        this.cowId = cowId;
    }

    public String getCowName() {
        return cowName;
    }

    public void setCowName(String cowName) {
        this.cowName = cowName;
    }

    public ArrayList<CowLocation> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<CowLocation> locations) {
        this.locations = locations;
    }

    public ArrayList<Double> getTemperatures() {
        return temperatures;
    }

    public void setTemperatures(ArrayList<Double> temperatures) {
        this.temperatures = temperatures;
    }

    public ArrayList<Long> getHeartRates() {
        return heartRates;
    }

    public void setHeartRates(ArrayList<Long> heartRates) {
        this.heartRates = heartRates;
    }

}
