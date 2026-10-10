package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Rider;
import java.util.List;

public class LeastActiveDriverStrategy implements RideMatchingStrategy {
    private final int maxRadius;

    public LeastActiveDriverStrategy(int maxRadius) {
        if (maxRadius <= 0) {
            throw new IllegalArgumentException("Radius must be strictly positive.");
        }
        this.maxRadius = maxRadius;
    }

    @Override
    public Driver findDriver(Rider rider, List<Driver> availableDrivers) {
        if (availableDrivers == null || availableDrivers.isEmpty()) {
            throw new NoDriverAvailableException("No drivers are currently available.");
        }

        Driver leastActiveDriver = null;
        int minRides = Integer.MAX_VALUE;

        for (Driver driver : availableDrivers) {
            int distance = Math.abs(driver.getCurrentLocation() - rider.getLocation());
            if (distance <= maxRadius && driver.getTotalRides() < minRides) {
                minRides = driver.getTotalRides();
                leastActiveDriver = driver;
            }
        }

        if (leastActiveDriver == null) {
            throw new NoDriverAvailableException("No drivers available within the " + maxRadius + " unit radius.");
        }
        return leastActiveDriver;
    }

    @Override
    public String getName() {
        return "Least Active Driver (Radius: " + maxRadius + ")";
    }
}
