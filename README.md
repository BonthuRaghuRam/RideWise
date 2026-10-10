# RideWise 

A simple console-based ride-sharing app written in Java, like a tiny Uber or Ola. You can add riders and drivers, book a ride, and get a fare.

This project was built to practice **clean code and object-oriented design** (OOP, SOLID, Strategy pattern).

## What it can do

- Add riders and drivers
- See which drivers are available
- Book a ride (the app picks a driver for you)
- Complete or cancel a ride
- Get a fare receipt
- Switch how drivers are picked and how fares are calculated, while the app is running

## How to run

You need the **JDK (Java 8 or newer)** installed.

**Using an IDE (IntelliJ, Eclipse, VS Code):**
1. Open the project folder.
2. Run `src/com/airtribe/ridewise/Main.java`.

## How to use it

When the app starts you will see this menu:

```
1. Add Rider
2. Add Driver
3. View Available Drivers
4. Request Ride
5. Complete Ride
6. View Rides
7. Cancel Ride
8. Exit
9. Change Strategies (Admin)
```

Type a number and press Enter. IDs are not case-sensitive (`r1` and `R1` both work).

### Example

```
Choose an option: 1
Enter Rider Name: Asha
Enter Location (number): 10
Success! Rider ID: R1

Choose an option: 2
Enter Driver Name: Ravi
Enter Location (number): 15
Enter Vehicle Type (BIKE, AUTO, CAR): car
Success! Driver ID: D1

Choose an option: 4
Enter Rider ID: R1
Enter Destination (number): 30
Ride requested! ID: TRIP1 assigned to: Ravi

Choose an option: 5
Enter Trip ID: TRIP1
Ride completed! Receipt Amount: Rs. 250.00
```

## How the fare is calculated

```
fare = (50 + distance × 10) × vehicle multiplier
```

| Vehicle | Multiplier |
|---------|-----------|
| BIKE | 0.5 |
| AUTO | 0.8 |
| CAR | 1.0 |

- **Distance** = how far the rider travels (pickup to destination).
- In the example above: distance is 20, so (50 + 20 × 10) × 1.0 = **250**.
- **Peak Hour pricing** (menu option 9) makes the fare 1.5 times higher.

## How a driver is picked

- **Nearest Driver:** picks the closest available driver.
- **Least Active Driver:** picks the driver who has completed the fewest rides.

Drivers more than 50 units away are never picked. If nobody is available, the ride is cancelled and you are told why.

## Ride status

```
REQUESTED → ASSIGNED → COMPLETED
                  ↘ CANCELLED
```

## Project structure

```
src/com/airtribe/ridewise/
├── Main.java          starts the app
├── console/           the menu (reads input, prints output)
├── model/             Rider, Driver, Ride, FareReceipt, RideStatus, VehicleType
├── strategy/          the ways to pick a driver and calculate a fare
├── service/           the main logic (RiderService, DriverService, RideService)
├── exception/         NoDriverAvailableException
└── util/              IdGenerator, Validator
```

## Design ideas used

| Idea | Where you can see it |
|------|----------------------|
| **Strategy pattern** | Driver matching and fare calculation can be swapped without changing `RideService` |
| **Single responsibility** | Models hold data, services hold logic, the menu only handles input and output |
| **Composition** | `RideService` uses strategies; `Ride` creates its own `FareReceipt` |
| **Validation** | Bad input (empty names, negative locations) is rejected early with a clear message |


## More details

See the `docs/` folder for the requirements, class model, object relationships and SOLID notes.

## Limitations

- Data is stored in memory only, so it is lost when you exit.
- No automated tests yet.
