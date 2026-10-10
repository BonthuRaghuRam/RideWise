package com.airtribe.ridewise.model;

public class Ride {
    private final String id;
    private final Rider rider;
    private Driver driver;
    private final int pickupLocation;
    private final int destination;
    private final int distance;
    private RideStatus status;
    private FareReceipt receipt;
    private String cancelReason;

    public Ride(String id, Rider rider, int destination) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Ride ID cannot be blank.");
        if (rider == null) throw new IllegalArgumentException("Rider cannot be null.");
        if (destination < 0) throw new IllegalArgumentException("Destination cannot be negative.");
        if (rider.getLocation() == destination) throw new IllegalArgumentException("Destination cannot be the same as pickup location.");

        this.id = id;
        this.rider = rider;
        this.pickupLocation = rider.getLocation();
        this.destination = destination;
        this.distance = Math.abs(destination - pickupLocation);
        this.status = RideStatus.REQUESTED;
    }

    public void requireInProgress() {
        if (status != RideStatus.ASSIGNED) {
            throw new IllegalStateException("Ride is not in progress.");
        }
    }

    public void assignDriver(Driver driver) {
        if (this.status != RideStatus.REQUESTED) throw new IllegalStateException("Ride must be in REQUESTED state to assign a driver.");
        if (driver == null) throw new IllegalArgumentException("Driver cannot be null.");
        this.driver = driver;
        this.status = RideStatus.ASSIGNED;
    }

    public void complete(double amount) {
        requireInProgress();

        // Build receipt first. If validation fails, state remains untouched.
        FareReceipt newReceipt = new FareReceipt(this.id, amount);

        this.receipt = newReceipt;
        this.status = RideStatus.COMPLETED;
        this.rider.setLocation(this.destination);
        this.driver.setCurrentLocation(this.destination);
        this.driver.incrementTotalRides();
    }

    public void cancel(String reason) {
        if (this.status == RideStatus.COMPLETED || this.status == RideStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel a completed or already cancelled ride.");
        }
        this.status = RideStatus.CANCELLED;
        this.cancelReason = reason;
    }

    public String getRiderId() { return rider.getId(); }
    public boolean hasDriver() { return driver != null; }
    public String getDriverId() { return hasDriver() ? driver.getId() : "Unassigned"; }
    public String getDriverName() { return hasDriver() ? driver.getName() : "Unassigned"; }
    public VehicleType getVehicleType() { return hasDriver() ? driver.getVehicleType() : null; }

    public String getId() { return id; }
    public int getDistance() { return distance; }
    public RideStatus getStatus() { return status; }
    public FareReceipt getReceipt() { return receipt; }
    public String getCancelReason() { return cancelReason; }
}
