package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;

public class PeakHourFareStrategy implements FareStrategy {
    private final FareStrategy baseStrategy;
    private final double surgeMultiplier;

    public PeakHourFareStrategy(FareStrategy baseStrategy, double surgeMultiplier) {
        if (baseStrategy == null) {
            throw new IllegalArgumentException("Base strategy cannot be null.");
        }
        if (surgeMultiplier <= 0) {
            throw new IllegalArgumentException("Surge multiplier must be strictly positive.");
        }
        this.baseStrategy = baseStrategy;
        this.surgeMultiplier = surgeMultiplier;
    }

    @Override
    public double calculateFare(Ride ride) {
        return baseStrategy.calculateFare(ride) * surgeMultiplier;
    }

    @Override
    public String getName() {
        return "Peak Hour Fare (" + surgeMultiplier + "x Surge, Base: " + baseStrategy.getName() + ")";
    }
}
