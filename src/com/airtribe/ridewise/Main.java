package com.airtribe.ridewise;

import com.airtribe.ridewise.console.ConsoleMenu;
import com.airtribe.ridewise.service.DriverService;
import com.airtribe.ridewise.service.RideService;
import com.airtribe.ridewise.service.RiderService;
import com.airtribe.ridewise.strategy.DefaultFareStrategy;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.LeastActiveDriverStrategy;
import com.airtribe.ridewise.strategy.NearestDriverStrategy;
import com.airtribe.ridewise.strategy.PeakHourFareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;

import java.util.LinkedHashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        RiderService riderService = new RiderService();
        DriverService driverService = new DriverService();

        Map<String, RideMatchingStrategy> matchingStrategies = new LinkedHashMap<>();
        matchingStrategies.put("1", new NearestDriverStrategy(50));
        matchingStrategies.put("2", new LeastActiveDriverStrategy(50));

        Map<String, FareStrategy> fareStrategies = new LinkedHashMap<>();
        fareStrategies.put("1", new DefaultFareStrategy(50.0, 10.0));
        fareStrategies.put("2", new PeakHourFareStrategy(new DefaultFareStrategy(50.0, 10.0), 1.5));

        RideService rideService = new RideService(
                riderService, driverService, matchingStrategies.get("1"), fareStrategies.get("1")
        );

        ConsoleMenu menu = new ConsoleMenu(riderService, driverService, rideService, matchingStrategies, fareStrategies);
        menu.start();
    }
}
