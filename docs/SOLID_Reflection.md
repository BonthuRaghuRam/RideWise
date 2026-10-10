# RideWise: SOLID and Design Principles Reflection

This document records where each principle is applied in the code, how, and where a deliberate trade-off was made.

## 1. SOLID

### S: Single Responsibility Principle

Each class has one reason to change.

| Class | Single responsibility |
|-------|-----------------------|
| `Rider`, `Driver`, `FareReceipt` | Hold and validate their own data |
| `Ride` | Own the ride state machine and its invariants |
| `RiderService` / `DriverService` | Registry and lookup for riders / drivers |
| `RideService` | Orchestrate the ride lifecycle (request, complete, cancel) |
| `NearestDriverStrategy`, `LeastActiveDriverStrategy` | One matching rule each |
| `DefaultFareStrategy`, `PeakHourFareStrategy` | One pricing rule each |
| `Validator` | Name validation rules |
| `IdGenerator` | ID generation |
| `ConsoleMenu` | Input, output and routing; no business rules |
| `Main` | Composition root only |

**Deliberate deviation from the brief:** the brief places the menu in `Main.java`. Here it lives in `console/ConsoleMenu` so that `Main` has only one job (wiring objects together), and the UI can be replaced (for example by a REST controller) without touching `Main`.

### O: Open/Closed Principle

The system is open for extension and closed for modification.

- **New matching rule** (for example "highest rated driver"): add a class implementing `RideMatchingStrategy`, register it in `Main`. `RideService`, the models and `ConsoleMenu` do not change.
- **New fare rule** (for example "night surcharge"): add a class implementing `FareStrategy`, register it in `Main`.
- **Composable pricing:** `PeakHourFareStrategy` wraps any `FareStrategy`, so new pricing layers can be stacked without editing existing strategies.
- **New vehicle type:** add one enum constant with its multiplier. No strategy changes are needed.
- **Configuration through constructors:** radius, base fare, per-unit rate and surge are injected rather than hardcoded in the algorithms.
- The admin menu builds its list from the registry maps, so a newly registered strategy appears automatically.

*Honest limit:* adding a strategy still requires one edit in `Main` to register it. That is the intended place for wiring (the composition root).

### L: Liskov Substitution Principle

Any implementation of a strategy interface can replace another without breaking `RideService`, because every implementation honours the same contract:

| Interface | Contract that all implementations keep |
|-----------|----------------------------------------|
| `RideMatchingStrategy.findDriver` | Returns a non-null driver from the supplied list, or throws `NoDriverAvailableException`. Never returns `null`. Does not modify the drivers |
| `FareStrategy.calculateFare` | Returns a non-negative fare for a ride with an assigned driver. Does not modify the ride |

Strategies are swapped at runtime through the admin menu, which demonstrates substitutability in practice. `PeakHourFareStrategy` is a true substitute for `DefaultFareStrategy`: callers cannot tell them apart.

### I: Interface Segregation Principle

Interfaces are small and focused:

- `RideMatchingStrategy`: `findDriver`, `getName`
- `FareStrategy`: `calculateFare`, `getName`

No implementation is forced to implement a method it does not need, and matching and pricing are separate interfaces rather than one large "RideStrategy".

### D: Dependency Inversion Principle

- `RideService` depends on the **interfaces** `RideMatchingStrategy` and `FareStrategy`, not on concrete algorithms. Concrete strategies are created in `Main` and injected.
- The `model` package has no dependency on `strategy` or `service`. Dependencies flow one way (see `Object_Relationships.md`).

**Conscious trade-off (YAGNI):** `RideService` depends on the concrete `RiderService` and `DriverService`. Abstracting them behind repository interfaces would only pay off if a second data store (database, cache) were planned. For an in-memory console engine it would add classes without adding value. If persistence is added later, extracting `RiderRepository` and `DriverRepository` interfaces is a small, local change.

## 2. Other Design Principles

### Composition over inheritance
There are no inheritance hierarchies of concrete classes. Behaviour is assembled by composition:
- `RideService` is composed of a matching strategy and a fare strategy.
- `Ride` owns its `FareReceipt`.
- `PeakHourFareStrategy` is composed of another `FareStrategy` (Decorator) rather than extending `DefaultFareStrategy`.

### DRY (Don't Repeat Yourself)

| Duplication removed | How |
|---------------------|-----|
| Name regex repeated for riders and drivers | Single `Validator.validateName` |
| Vehicle multiplier `switch` copied into each fare strategy | Multiplier lives once in `VehicleType` |
| Base fare formula copied into the peak strategy | `PeakHourFareStrategy` delegates to the wrapped strategy |
| "Find ride or throw" repeated in complete and cancel | Private `getRideOrThrow` |
| "Ride must be in progress" check | `Ride.requireInProgress()` |

### KISS (Keep It Simple)
- Locations are plain integers on a line; distance is `Math.abs`.
- Double-booking prevention is a `HashSet<String>` lookup instead of scanning the ride history.
- Plain Java with no framework, in-memory `LinkedHashMap` storage.

### YAGNI (You Aren't Gonna Need It)
- No repository interfaces, persistence layer, or concurrency control.
- No payments, ratings or authentication.
- Only two strategies per axis, enough to prove the extension points.

### Law of Demeter
Services and the UI talk to their direct collaborators and avoid reaching through objects:

| Instead of | The code uses |
|------------|---------------|
| `ride.getDriver().getName()` | `ride.getDriverName()` |
| `ride.getDriver().getId()` | `ride.getDriverId()` |
| `ride.getRider().getId()` | `ride.getRiderId()` |
| `ride.getDriver().getVehicleType()` | `ride.getVehicleType()` |

`Ride` does not expose its `Rider` or `Driver` objects at all.

### Low coupling and high cohesion
- **Low coupling:** services interact through narrow methods; strategies know only `Rider`, `Driver` and `Ride`; the model knows nothing about the layers above it.
- **High cohesion:** every class's members serve one purpose (see the SRP table).

### Fail-fast validation
Constructors reject invalid data (blank IDs, bad names, negative locations, non-positive radius or surge, null dependencies), so an invalid object cannot exist. `Ride.complete` builds the receipt **before** changing any state, so a failure cannot leave a ride half-completed.

## 3. Known Trade-offs and Possible Future Work

| Topic | Current state | Possible improvement |
|-------|---------------|----------------------|
| Mutators | `Driver.setAvailable`, `incrementTotalRides`, `setCurrentLocation` and `Rider.setLocation` are public so that `Ride` and the services (in other packages) can call them | Package-private access with `Ride` and the services in a shared package, or intention-revealing methods such as `markBusy()` / `markFree()` |
| Driver availability | `RideService` flips availability after `Ride.assignDriver` | Let `Ride.assignDriver` mark the driver busy, so the two cannot drift apart |
| Service abstraction | `RideService` uses concrete `RiderService` / `DriverService` | Repository interfaces if persistence is introduced |
| Strategy registration | Registered manually in `Main` | A small `StrategyRegistry` class or configuration file |
| Testing | No automated tests yet | Unit tests for strategies, `Ride` transitions, fare maths and double-booking |
| Concurrency | Single-threaded console | Synchronise or use concurrent collections if multi-user access is needed |