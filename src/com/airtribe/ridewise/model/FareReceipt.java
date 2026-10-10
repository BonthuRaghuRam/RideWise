package com.airtribe.ridewise.model;

import java.time.LocalDateTime;

public class FareReceipt {
    private final String rideId;
    private final double amount;
    private final LocalDateTime generatedAt;

    public FareReceipt(String rideId, double amount) {
        if (rideId == null || rideId.trim().isEmpty()) {
            throw new IllegalArgumentException("Ride ID cannot be empty.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Fare amount cannot be negative.");
        }

        this.rideId = rideId;
        this.amount = amount;
        this.generatedAt = LocalDateTime.now();
    }

    public String getRideId() { return rideId; }
    public double getAmount() { return amount; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
}
