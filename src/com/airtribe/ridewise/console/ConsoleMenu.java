package com.airtribe.ridewise.console;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.FareReceipt;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.service.DriverService;
import com.airtribe.ridewise.service.RideService;
import com.airtribe.ridewise.service.RiderService;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleMenu {
    private final RiderService riderService;
    private final DriverService driverService;
    private final RideService rideService;
    private final Scanner scanner;

    private final Map<String, RideMatchingStrategy> matchingStrategies;
    private final Map<String, FareStrategy> fareStrategies;

    public ConsoleMenu(RiderService riderService, DriverService driverService, RideService rideService,
                       Map<String, RideMatchingStrategy> matchingStrategies, Map<String, FareStrategy> fareStrategies) {
        this.riderService = riderService;
        this.driverService = driverService;
        this.rideService = rideService;
        this.matchingStrategies = matchingStrategies;
        this.fareStrategies = fareStrategies;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to RideWise!");

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Add Rider");
            System.out.println("2. Add Driver");
            System.out.println("3. View Available Drivers");
            System.out.println("4. Request Ride");
            System.out.println("5. Complete Ride");
            System.out.println("6. View Rides");
            System.out.println("7. Cancel Ride");
            System.out.println("8. Exit");
            System.out.println("9. Change Strategies (Admin)");
            System.out.print("Choose an option: ");

            String input;
            try {
                input = scanner.nextLine().trim();
            } catch (java.util.NoSuchElementException e) {
                System.out.println("\nInput closed. Exiting.");
                return;
            }

            try {
                switch (input) {
                    case "1":
                        System.out.print("Enter Rider Name: ");
                        String rName = scanner.nextLine().trim();

                        System.out.print("Enter Location (number): ");
                        int rLoc = Integer.parseInt(scanner.nextLine().trim());

                        Rider rider = riderService.registerRider(rName, rLoc);
                        System.out.println("Success! Rider ID: " + rider.getId());
                        break;

                    case "2":
                        System.out.print("Enter Driver Name: ");
                        String dName = scanner.nextLine().trim();

                        System.out.print("Enter Location (number): ");
                        int dLoc = Integer.parseInt(scanner.nextLine().trim());

                        System.out.print("Enter Vehicle Type (BIKE, AUTO, CAR): ");
                        VehicleType vType;
                        try {
                            vType = VehicleType.valueOf(scanner.nextLine().trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            System.out.println("Error: Choose BIKE, AUTO, or CAR.");
                            continue;
                        }

                        Driver driver = driverService.registerDriver(dName, dLoc, vType);
                        System.out.println("Success! Driver ID: " + driver.getId());
                        break;

                    case "3":
                        List<Driver> available = driverService.getAvailableDrivers();
                        System.out.println("Available Drivers: " + available.size());
                        for (Driver d : available) {
                            System.out.println(d.getId() + " - " + d.getName() + " [" + d.getVehicleType() + "] (Location: " + d.getCurrentLocation() + ")");
                        }
                        break;

                    case "4":
                        System.out.print("Enter Rider ID: ");
                        String reqRiderId = scanner.nextLine().trim().toUpperCase();

                        System.out.print("Enter Destination (number): ");
                        int destination = Integer.parseInt(scanner.nextLine().trim());

                        Ride ride = rideService.requestRide(reqRiderId, destination);
                        System.out.println("Ride requested! Trip ID: " + ride.getId() + " assigned to: " + ride.getDriverName());                        break;

                    case "5":
                        System.out.print("Enter Trip ID (e.g. TRIP1): ");
                        String reqRideId = scanner.nextLine().trim().toUpperCase();
                        FareReceipt receipt = rideService.completeRide(reqRideId);
                        System.out.println("Ride completed! Receipt Amount: Rs. " + String.format("%.2f", receipt.getAmount()));
                        break;

                    case "6":
                        List<Ride> allRides = rideService.getAllRides();
                        if (allRides.isEmpty()) {
                            System.out.println("No rides yet.");
                        } else {
                            for (Ride r : allRides) {
                                String reason = r.getCancelReason() != null ? " (" + r.getCancelReason() + ")" : "";
                                System.out.println("Trip " + r.getId() + " | Status: " + r.getStatus() + reason + " | Driver: " + r.getDriverName());
                            }
                        }
                        break;

                    case "7":
                        System.out.print("Enter Trip ID to cancel (e.g. TRIP1): ");
                        String cancelRideId = scanner.nextLine().trim().toUpperCase();
                        rideService.cancelRide(cancelRideId);
                        System.out.println("Ride " + cancelRideId + " has been successfully cancelled.");
                        break;

                    case "8":
                        System.out.println("Thank you for using RideWise. Goodbye!");
                        return;

                    case "9":
                        System.out.println("\n--- Strategy Settings ---");

                        System.out.println("Available Matching Strategies:");
                        matchingStrategies.forEach((key, strategy) ->
                                System.out.println(key + ". " + strategy.getName()));
                        System.out.print("Choose matching strategy (or press enter to skip): ");

                        String matchChoice = scanner.nextLine().trim();
                        if (!matchChoice.isEmpty()) {
                            if (matchingStrategies.containsKey(matchChoice)) {
                                rideService.setMatchingStrategy(matchingStrategies.get(matchChoice));
                                System.out.println("Matching strategy updated to: " + matchingStrategies.get(matchChoice).getName());
                            } else {
                                System.out.println("Invalid choice. Strategy not changed.");
                            }
                        }

                        System.out.println("\nAvailable Fare Strategies:");
                        fareStrategies.forEach((key, strategy) ->
                                System.out.println(key + ". " + strategy.getName()));
                        System.out.print("Choose fare strategy (or press enter to skip): ");

                        String fareChoice = scanner.nextLine().trim();
                        if (!fareChoice.isEmpty()) {
                            if (fareStrategies.containsKey(fareChoice)) {
                                rideService.setFareStrategy(fareStrategies.get(fareChoice));
                                System.out.println("Fare strategy updated to: " + fareStrategies.get(fareChoice).getName());
                            } else {
                                System.out.println("Invalid choice. Strategy not changed.");
                            }
                        }
                        break;

                    default:
                        System.out.println("Error: '" + input + "' is not a valid menu option. Please type a single number from 1 to 9.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid whole number.");
            } catch (IllegalArgumentException | IllegalStateException | NoDriverAvailableException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected Error: " + e.getMessage());
            }
        }
    }
}
