# RideWise: System Requirements

## 1. Purpose

RideWise is a simplified, console-based ride-sharing engine (Uber/Ola style). It is **not** a full application. Its goal is to demonstrate high-quality low-level design: clean layering, the Strategy pattern, SOLID principles, composition over inheritance, low coupling and high cohesion.

## 2. Scope

**In scope**
- Registering riders and drivers on a one-dimensional integer grid
- Listing available drivers
- Requesting a ride and matching a driver through an interchangeable strategy
- Pricing a ride through an interchangeable strategy
- Tracking the ride lifecycle (`REQUESTED`, `ASSIGNED`, `COMPLETED`, `CANCELLED`)
- Cancelling rides
- Switching matching and pricing strategies at runtime

**Out of scope (YAGNI)**
- Persistence or a database (all data is in memory)
- Real maps, GPS or 2-D coordinates
- Payments, ratings, authentication
- Concurrency or multi-user access (the console is single-threaded)

## 3. Functional Requirements

| ID | Requirement |
|----|-------------|
| FR-1 | Register a **rider** with a name and a location. The system generates an ID of the form `R<n>`. |
| FR-2 | Register a **driver** with a name, a location and a `VehicleType` (`BIKE`, `AUTO`, `CAR`). The system generates an ID of the form `D<n>`. |
| FR-3 | List all **available** drivers with ID, name, vehicle type and location. |
| FR-4 | **Request a ride** for a rider ID and a destination. The ride is created in `REQUESTED` state and stored immediately. |
| FR-5 | **Match a driver** using the active `RideMatchingStrategy`. On success the ride becomes `ASSIGNED` and the driver becomes unavailable. |
| FR-6 | If no driver can be matched, the ride is automatically `CANCELLED` with the reason "No drivers available" and the user is told why. |
| FR-7 | **Complete a ride.** The fare is calculated by the active `FareStrategy`, a `FareReceipt` is generated, the rider and driver move to the destination, the driver's ride count increases and the driver becomes available again. |
| FR-8 | **Cancel a ride** that is `REQUESTED` or `ASSIGNED`. A driver, if assigned, becomes available again. A cancel reason is stored. |
| FR-9 | **View all rides** with ID, status, cancel reason (if any) and driver name. |
| FR-10 | **Change strategies at runtime** (matching and fare, each optional) through an admin menu option. |
| FR-11 | Prevent a rider from having more than one active ride at a time ("double booking"). |
| FR-12 | Reject invalid input with a clear message and keep the application running. |

## 4. Non-Functional Requirements

| ID | Requirement |
|----|-------------|
| NFR-1 | **Extensible pricing:** a new fare algorithm must be addable by creating one new class and registering it in `Main`, with no change to `RideService`. |
| NFR-2 | **Swappable matching:** the driver-matching logic can be changed without touching the services or the model. |
| NFR-3 | **Low coupling:** services depend on strategy interfaces; the model does not depend on strategies or services. |
| NFR-4 | **Separation of concerns:** all business logic is free of console I/O. `ConsoleMenu` only reads input, calls services and prints results. |
| NFR-5 | **Determinism:** equidistant drivers, or drivers with equal ride counts, are resolved by registration order (earliest registered wins). |
| NFR-6 | **Performance:** the double-booking check is O(1) using a `HashSet` of active rider IDs. |
| NFR-7 | **Fail-fast validation:** invalid state is rejected in constructors and strategy constructors, so objects cannot exist in an invalid state. |
| NFR-8 | **Maintainability:** small, single-purpose classes and readable code. |

## 5. Domain Rules and Validation

| Rule | Where enforced |
|------|----------------|
| Names must be non-blank and contain only letters and spaces | `Validator.validateName`, called from `Rider` and `Driver` |
| IDs must be non-blank | `Rider`, `Driver`, `Ride`, `FareReceipt` constructors |
| Locations must be non-negative integers (`0` is allowed) | `Rider`, `Driver` constructors |
| Destination must be non-negative and different from the pickup location | `Ride` constructor |
| A driver must have a vehicle type | `Driver` constructor |
| Matching radius must be strictly positive | `NearestDriverStrategy`, `LeastActiveDriverStrategy` constructors |
| Fare rates must be non-negative; surge multiplier must be strictly positive; surge base strategy must not be null | `DefaultFareStrategy`, `PeakHourFareStrategy` constructors |
| A fare amount cannot be negative | `FareReceipt` constructor |
| A rider can have only one active ride | `RideService.activeRiders` |

## 6. Matching Rules

- Only **available** drivers are considered.
- A driver is eligible only if `|driverLocation - riderLocation| <= maxRadius` (default radius: **50**).
- **Nearest Driver:** pick the eligible driver with the smallest distance to the rider.
- **Least Active Driver:** pick the eligible driver with the fewest completed rides.
- Ties are broken by registration order.
- If the list is empty, or no driver is within the radius, `NoDriverAvailableException` is thrown.

## 7. Fare Rules

```
Default fare   = (baseFare + distance × perKmRate) × vehicleMultiplier
Peak-hour fare = Default fare × surgeMultiplier
```

| Parameter | Default value |
|-----------|---------------|
| `baseFare` | 50.0 |
| `perKmRate` | 10.0 |
| `surgeMultiplier` | 1.5 |

| Vehicle type | Multiplier |
|--------------|-----------|
| `BIKE` | 0.5 |
| `AUTO` | 0.8 |
| `CAR` | 1.0 |

`distance` is the trip distance from **pickup to destination**, not the driver's approach distance. It is fixed when the ride is created.

**Example:** a CAR ride of 10 units under the default strategy costs (50 + 10 × 10) × 1.0 = **Rs. 150.00**. Under peak hour it costs 150 × 1.5 = **Rs. 225.00**.

## 8. Ride Lifecycle

```
REQUESTED ──assign driver──► ASSIGNED ──complete──► COMPLETED
    │                            │
    └──────────cancel────────────┴──────────────────► CANCELLED
```

| From | Action | To |
|------|--------|----|
| `REQUESTED` | `assignDriver` | `ASSIGNED` |
| `REQUESTED` | `cancel` (also used when no driver is found) | `CANCELLED` |
| `ASSIGNED` | `complete` | `COMPLETED` |
| `ASSIGNED` | `cancel` | `CANCELLED` |
| `COMPLETED` / `CANCELLED` | any | rejected with `IllegalStateException` |

State changes are only possible through methods on `Ride`; there is no public `setStatus`.

## 9. Console Menu

| Option | Action | Inputs |
|--------|--------|--------|
| 1 | Add Rider | name, location |
| 2 | Add Driver | name, location, vehicle type |
| 3 | View Available Drivers | none |
| 4 | Request Ride | rider ID, destination |
| 5 | Complete Ride | ride ID |
| 6 | View Rides | none |
| 7 | Cancel Ride | ride ID |
| 8 | Exit | none |
| 9 | Change Strategies (Admin) | matching choice and/or fare choice (Enter skips) |

Menu rules:
- Input is trimmed, and IDs are upper-cased, so `r1` and ` R1 ` both work.
- A non-numeric value where a number is expected shows "Please enter a valid whole number."
- Business-rule errors (`IllegalArgumentException`, `IllegalStateException`, `NoDriverAvailableException`) are shown as `Error: <message>`.
- If standard input is closed, the menu exits cleanly instead of looping.
