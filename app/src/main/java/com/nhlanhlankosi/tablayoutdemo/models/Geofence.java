package com.nhlanhlankosi.tablayoutdemo.models;

import java.util.List;

public class Geofence {
    private String name;
    private List<LatiLongi> coordinates;
    private int color;

    public Geofence() {
    }

    public Geofence(String name, List<LatiLongi> coordinates, int color) {
        this.name = name;
        this.coordinates = coordinates;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public List<LatiLongi> getCoordinates() {
        return coordinates;
    }

    public int getColor() {
        return color;
    }
}
