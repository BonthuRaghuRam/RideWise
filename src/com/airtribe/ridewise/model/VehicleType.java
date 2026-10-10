package com.airtribe.ridewise.model;

public enum VehicleType {
    BIKE(0.5),
    AUTO(0.8),
    CAR(1.0);

    private final double fareMultiplier;

    VehicleType(double fareMultiplier) {
        this.fareMultiplier = fareMultiplier;
    }

    public double getFareMultiplier() {
        return fareMultiplier;
    }
}
