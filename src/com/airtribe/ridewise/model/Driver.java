package com.airtribe.ridewise.model;

import com.airtribe.ridewise.util.Validator;

public class Driver {
    private final String id;
    private final String name;
    private int currentLocation;
    private final VehicleType vehicleType;
    private boolean available;
    private int totalRides;

    public Driver(String id, String name, int currentLocation, VehicleType vehicleType) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver ID cannot be blank.");
        }
        Validator.validateName(name, "Driver");
        if (currentLocation < 0) {
            throw new IllegalArgumentException("Location cannot be negative.");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type cannot be null.");
        }
        this.id = id;
        this.name = name;
        this.currentLocation = currentLocation;
        this.vehicleType = vehicleType;
        this.available = true;
        this.totalRides = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(int location) { this.currentLocation = location; }
    public VehicleType getVehicleType() { return vehicleType; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public int getTotalRides() { return totalRides; }
    public void incrementTotalRides() { this.totalRides++; }
}
