package com.nhlanhlankosi.tablayoutdemo.models;

import java.util.ArrayList;
import java.util.Random;

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

    public static CowSensorData createRandomSensorData(String cowId, String cowName, int numEntries) {
        Random random = new Random();

        ArrayList<CowLocation> locations = new ArrayList<>();
        ArrayList<Double> temperatures = new ArrayList<>();
        ArrayList<Long> heartRates = new ArrayList<>();

        for (int i = 0; i < numEntries; i++) {
            // Random location - assuming CowLocation has a constructor that takes random coordinates
            locations.add(new CowLocation(random.nextDouble() * 100, random.nextDouble() * 100));

            // Random temperature between 35.0 and 40.0 degrees Celsius
            temperatures.add(35.0 + (40.0 - 35.0) * random.nextDouble());

            // Random heart rate between 40 and 120 BPM
            heartRates.add(40L + (long)(random.nextInt(81)));
        }

        return new CowSensorData(cowId, cowName, locations, temperatures, heartRates);
    }

}
