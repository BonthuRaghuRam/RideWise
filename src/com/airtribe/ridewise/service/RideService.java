package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.FareReceipt;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;
import com.airtribe.ridewise.util.IdGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RideService {
    private final RiderService riderService;
    private final DriverService driverService;
    private RideMatchingStrategy matchingStrategy;
    private FareStrategy fareStrategy;

    private final Map<String, Ride> rides = new LinkedHashMap<>();
    private final Set<String> activeRiders = new HashSet<>();
    private final IdGenerator idGenerator = new IdGenerator();

    public RideService(RiderService riderService, DriverService driverService,
                       RideMatchingStrategy matchingStrategy, FareStrategy fareStrategy) {
        this.riderService = riderService;
        this.driverService = driverService;
        this.matchingStrategy = matchingStrategy;
        this.fareStrategy = fareStrategy;
    }

    public void setMatchingStrategy(RideMatchingStrategy matchingStrategy) { this.matchingStrategy = matchingStrategy; }
    public void setFareStrategy(FareStrategy fareStrategy) { this.fareStrategy = fareStrategy; }

    public Ride requestRide(String riderId, int destination) {
        Rider rider = riderService.getRider(riderId);

        if (activeRiders.contains(riderId)) {
            throw new IllegalStateException("Rider already has an active ride in progress.");
        }

        String rideId = "TRIP" + idGenerator.generateId();
        Ride ride = new Ride(rideId, rider, destination);
        rides.put(rideId, ride);

        List<Driver> availableDrivers = driverService.getAvailableDrivers();
        Driver matchedDriver;

        try {
            matchedDriver = matchingStrategy.findDriver(rider, availableDrivers);
        } catch (NoDriverAvailableException e) {
            ride.cancel("No drivers available");
            throw e;
        }

        ride.assignDriver(matchedDriver);
        driverService.updateAvailability(matchedDriver.getId(), false);
        activeRiders.add(riderId);

        return ride;
    }

    public FareReceipt completeRide(String rideId) {
        Ride ride = getRideOrThrow(rideId);

        ride.requireInProgress();

        double amount = fareStrategy.calculateFare(ride);
        ride.complete(amount);

        driverService.updateAvailability(ride.getDriverId(), true);
        activeRiders.remove(ride.getRiderId());

        return ride.getReceipt();
    }

    public void cancelRide(String rideId) {
        Ride ride = getRideOrThrow(rideId);

        ride.cancel("User requested cancellation");
        if (ride.hasDriver()) {
            driverService.updateAvailability(ride.getDriverId(), true);
        }
        activeRiders.remove(ride.getRiderId());
    }

    public List<Ride> getAllRides() { return new ArrayList<>(rides.values()); }

    private Ride getRideOrThrow(String rideId) {
        if (!rides.containsKey(rideId)) throw new IllegalArgumentException("Trip not found with ID: " + rideId);
        return rides.get(rideId);
    }
}
