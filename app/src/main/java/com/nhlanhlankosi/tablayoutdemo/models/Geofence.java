package com.nhlanhlankosi.tablayoutdemo.models;

import java.util.List;

public class Geofence {

    private String id;
    private String name;
    private List<LatiLongi> coordinates;
    private int color;

    public Geofence() {
    }

    public Geofence(String id, String name, List<LatiLongi> coordinates, int color) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LatiLongi> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<LatiLongi> coordinates) {
        this.coordinates = coordinates;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
