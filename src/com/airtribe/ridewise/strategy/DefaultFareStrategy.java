package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;

public class DefaultFareStrategy implements FareStrategy {
    private final double baseFare;
    private final double perKmRate;

    public DefaultFareStrategy(double baseFare, double perKmRate) {
        if (baseFare < 0 || perKmRate < 0) {
            throw new IllegalArgumentException("Rates cannot be negative.");
        }
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
    }

    @Override
    public double calculateFare(Ride ride) {
        if (!ride.hasDriver()) throw new IllegalStateException("Cannot calculate fare without an assigned vehicle.");
        return (baseFare + (ride.getDistance() * perKmRate)) * ride.getVehicleType().getFareMultiplier();
    }

    @Override
    public String getName() {
        return "Default Fare (Base: " + baseFare + ", Per Km: " + perKmRate + ")";
    }
}
