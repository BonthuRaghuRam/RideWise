package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.util.IdGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DriverService {
    private final Map<String, Driver> drivers = new LinkedHashMap<>();
    private final IdGenerator idGenerator = new IdGenerator();

    public Driver registerDriver(String name, int location, VehicleType vehicleType) {
        String id = "D" + idGenerator.generateId();
        Driver driver = new Driver(id, name, location, vehicleType);
        drivers.put(id, driver);
        return driver;
    }

    public List<Driver> getAvailableDrivers() {
        List<Driver> available = new ArrayList<>();
        for (Driver driver : drivers.values()) {
            if (driver.isAvailable()) {
                available.add(driver);
            }
        }
        return available;
    }

    public void updateAvailability(String id, boolean status) {
        if (!drivers.containsKey(id)) {
            throw new IllegalArgumentException("Driver not found with ID: " + id);
        }
        drivers.get(id).setAvailable(status);
    }
}
