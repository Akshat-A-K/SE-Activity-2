# Factory Pattern - Before Implementation

## Overview
This document describes the current state of the codebase **before** applying the Factory Pattern, highlighting the structural problems and design flaws that make the Factory Pattern necessary.

## Current Architecture Problems

### 1. **No Aircraft Abstraction**
The codebase has three separate aircraft classes with no common interface or base class:
- `PassengerPlane`
- `Helicopter`
- `PassengerDrone`

```java
// No common interface or abstract class exists
public class PassengerPlane {
    public String model;
    public int passengerCapacity;
    public int crewCapacity;
}

public class Helicopter {
    private String model;
    private int passengerCapacity;
}

public class PassengerDrone {
    private String model;
}
```

### 2. **Object Type Storage**
The `Flight` class stores aircraft as a generic `Object` type, requiring extensive type casting:

```java
public class Flight {
    protected Object aircraft;  //   No type safety
    
    public Flight(int number, Airport departure, Airport arrival, Object aircraft) {
        this.aircraft = aircraft;
    }
}
```

### 3. **Repetitive Type Checking**
Multiple methods require `instanceof` checks to determine aircraft type:

```java
private boolean isAircraftValid(Airport airport) {
    return Arrays.stream(airport.getAllowedAircrafts()).anyMatch(x -> {
        String model;
        if (this.aircraft instanceof PassengerPlane) {
            model = ((PassengerPlane) this.aircraft).model;
        } else if (this.aircraft instanceof Helicopter) {
            model = ((Helicopter) this.aircraft).getModel();
        } else if (this.aircraft instanceof PassengerDrone) {
            model = "HypaHype";  //   Hard-coded value
        } else {
            throw new IllegalArgumentException("Aircraft is not recognized");
        }
        return x.equals(model);
    });
}
```

```java
public int getCapacity() {
    if (aircraft instanceof PassengerPlane) {
        return ((PassengerPlane) aircraft).passengerCapacity;
    } else if (aircraft instanceof Helicopter) {
        return ((Helicopter) aircraft).getPassengerCapacity();
    } else if (aircraft instanceof PassengerDrone) {
        return 1;  //   Hard-coded capacity
    }
    return 0;
}
```

### 4. **Hard-coded Aircraft Models**
Aircraft configurations are hard-coded in constructors using switch statements:

```java
public PassengerPlane(String model) {
    this.model = model;
    switch (model) {
        case "A380":
            this.passengerCapacity = 853;
            this.crewCapacity = 20;
            break;
        case "A350":
            this.passengerCapacity = 440;
            this.crewCapacity = 15;
            break;
        case "Embraer 190":
            this.passengerCapacity = 114;
            this.crewCapacity = 5;
            break;
        case "Antonov AN2":
            this.passengerCapacity = 12;
            this.crewCapacity = 2;
            break;
        default:
            throw new IllegalArgumentException("Unknown model");
    }
}
```

### 5. **Direct Instantiation**
Aircraft objects are created directly throughout the codebase:

```java
// No centralized creation logic
PassengerPlane plane = new PassengerPlane("A380");
Helicopter heli = new Helicopter("Bell 407");
PassengerDrone drone = new PassengerDrone("HypaHype");
```

## Key Design Violations

### Open/Closed Principle  
- Adding a new aircraft type requires modifying existing code in multiple places
- `Flight.isAircraftValid()` must be updated
- `ScheduledFlight.getCapacity()` must be updated
- All instanceof checks must be extended

### Single Responsibility Principle  
- `Flight` class handles aircraft validation logic
- `PassengerPlane` constructor contains configuration data
- Business logic mixed with data

### Dependency Inversion Principle  
- High-level `Flight` class depends on concrete implementations
- No abstraction layer between Flight and aircraft types

### Don't Repeat Yourself (DRY)  
- Type checking logic duplicated across methods
- Model configurations duplicated in constructors

## Problems Illustrated in Class Diagram

```
Flight
├── aircraft: Object    No type safety
├── isAircraftValid()   instanceof checks
└── getCapacity()       instanceof checks

PassengerPlane
└── constructor         Switch statement for models

Helicopter
└── constructor         Hard-coded configuration

PassengerDrone
└── model              Hard-coded "HypaHype"
```

## Impact on Maintainability

### Adding New Aircraft Type Requires:
1.     Create new aircraft class
2.     Modify `Flight.isAircraftValid()`
3.     Modify `ScheduledFlight.getCapacity()`
4.     Modify `ScheduledFlight.getCrewMemberCapacity()`
5.     Update all instanceof checks
6.     Add switch case for new model

**Result**: Changes in 5+ locations for a single new aircraft type!

### Code Smells Identified
-    **Type Casting**: Excessive use of `(PassengerPlane)`, `(Helicopter)` casts
-    **Magic Numbers**: Hard-coded capacities (853, 440, 114, 12, 1)
-    **Magic Strings**: Hard-coded models ("A380", "HypaHype")
-    **Long Method**: `isAircraftValid()` with nested conditionals
-    **Switch Statements**: Aircraft configuration logic
-    **Primitive Obsession**: Using `Object` instead of proper types

## Testing Challenges

```java
// Hard to mock aircraft behavior
@Test
public void testFlightCapacity() {
    Object aircraft = new PassengerPlane("A380");  //   Object type
    Flight flight = new Flight(101, jfk, lax, aircraft);
    
    // Must cast to access methods
    int capacity = ((PassengerPlane) aircraft).passengerCapacity;
    assertEquals(853, capacity);
}
```

## Performance Issues
- Runtime type checking (`instanceof`) instead of polymorphism
- No compile-time type safety
- Potential for ClassCastException at runtime

## Current Class Structure

```
flight.reservation
├── Customer
├── Passenger
└── Airport

flight.reservation.flight
├── Flight (uses Object aircraft)
└── ScheduledFlight (extends Flight)

flight.reservation.plane
├── PassengerPlane (no interface)
├── Helicopter (no interface)
└── PassengerDrone (no interface)

  No common abstraction
  No factory for creation
  No polymorphic behavior
```

## Why Factory Pattern is Needed

The Factory Pattern will solve these problems by:

1. **Creating Aircraft Abstraction**: Introduce `Aircraft` interface/abstract class
2. **Centralizing Creation Logic**: Use factory to create aircraft instances
3. **Enabling Polymorphism**: Replace instanceof checks with polymorphic methods
4. **Separating Configuration**: Move hard-coded values to factory or configuration
5. **Type Safety**: Change `Object aircraft` to `Aircraft aircraft`
6. **Open/Closed Compliance**: New aircraft types won't require modifying existing code

## Metrics

| Metric | Current State |
|--------|---------------|
| instanceof checks | 6+ occurrences |
| Type casts | 8+ occurrences |
| Switch statements | 2+ for configuration |
| Classes to modify for new aircraft | 5+ classes |
| Code duplication | High |
| Type safety | None (Object type) |
| Testability | Low (hard to mock) |

## Conclusion

The current implementation suffers from:
-   Poor abstraction
-   Tight coupling
-   Code duplication
-   Violation of SOLID principles
-   Low maintainability
-   Poor testability

**The Factory Pattern is essential to resolve these architectural issues and create a maintainable, extensible aircraft management system.**

---

**Next Step**: Review `Factory_after.puml` to see how the Factory Pattern resolves all these issues.