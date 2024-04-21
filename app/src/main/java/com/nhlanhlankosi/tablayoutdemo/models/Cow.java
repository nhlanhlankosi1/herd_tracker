package com.nhlanhlankosi.tablayoutdemo.models;

public class Cow {
    private String name;
    private String id;
    private String cowPicUrl = "";
    private String collarId;
    private String gender;
    private String breed;
    private long heartRate;

    private double temperature;

    private double longitude;
    private double latitude;

    public Cow(String name, String id, String cowPicUrl, String collarId, String gender,
               String breed, long heartRate, double temperature, double longitude, double latitude) {
        this.name = name;
        this.id = id;
        this.cowPicUrl = cowPicUrl;
        this.collarId = collarId;
        this.gender = gender;
        this.breed = breed;
        this.heartRate = heartRate;
        this.temperature = temperature;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCollarId() {
        return collarId;
    }

    public void setCollarId(String collarId) {
        this.collarId = collarId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public long getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(long heartRate) {
        this.heartRate = heartRate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCowPicUrl() {
        return cowPicUrl;
    }

    public void setCowPicUrl(String cowPicUrl) {
        this.cowPicUrl = cowPicUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
}
