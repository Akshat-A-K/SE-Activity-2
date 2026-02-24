# Assignment 2 — Design Patterns Implementation (Version 1)

**Course:** S26CS6.401 — Software Engineering  
**Team No:** ___  
**Team Members:**  
- Parv Shah (Roll No: ___)
- ___ (Roll No: ___)
- ___ (Roll No: ___)

**Repository:** https://github.com/Sidx-sys/Reservation-System-Starter

---

## Table of Contents
1. [Codebase Overview](#1-codebase-overview)
2. [Code Smells Identified](#2-code-smells-identified)
3. [Code Quality Metrics — Before (Baseline)](#3-code-quality-metrics--before-baseline)
4. [Design Patterns Identified](#4-design-patterns-identified)
   - 4.1 [Adapter Pattern](#41-adapter-pattern)
   - 4.2 [Factory Pattern](#42-factory-pattern)
   - 4.3 [Strategy Pattern](#43-strategy-pattern)
   - 4.4 [Builder Pattern](#44-builder-pattern)
   - 4.5 [Observer Pattern](#45-observer-pattern)
   - 4.6 [Chain of Responsibility Pattern](#46-chain-of-responsibility-pattern)
5. [Summary & Next Steps](#5-summary--next-steps)

---

## 1. Codebase Overview

The codebase is a **Flight Reservation System** that allows customers to book passengers on scheduled flights and pay via credit card or PayPal. It has **14 classes** across 5 packages:

| Package | Classes | Responsibility |
|---------|---------|---------------|
| `(default)` | `Runner` | Entry point, creates sample data |
| `flight.reservation` | `Airport`, `Customer`, `Passenger` | Domain entities |
| `flight.reservation.flight` | `Flight`, `ScheduledFlight`, `Schedule` | Flight management |
| `flight.reservation.order` | `Order`, `FlightOrder` | Order creation & payment processing |
| `flight.reservation.payment` | `CreditCard`, `Paypal` | Payment details |
| `flight.reservation.plane` | `PassengerPlane`, `Helicopter`, `PassengerDrone` | Aircraft types |

**Flow:**  
`Customer` → creates `FlightOrder` with passengers on `ScheduledFlight`(s) → processes payment (CreditCard/PayPal) → order is closed.

---

## 2. Code Smells Identified

These were detected using **DesigniteJava** and **Checkstyle** static analysis tools.

### 2.1 Design Smells (from DesigniteJava)

| Class | Design Smell |
|-------|-------------|
| `Customer` | Cyclic-Dependent Modularization |
| `Passenger` | Unutilized Abstraction |
| `ScheduledFlight` | Broken Hierarchy, Missing Hierarchy |
| `FlightOrder` | Unutilized Abstraction, Broken Hierarchy, Cyclic-Dependent Modularization |
| `Order` | Unutilized Abstraction |
| `Paypal` | Unnecessary Abstraction, Deficient Encapsulation |
| `Helicopter` | Unutilized Abstraction |
| `PassengerDrone` | Unutilized Abstraction |
| `PassengerPlane` | Unutilized Abstraction, Deficient Encapsulation |
| `Runner` | Unutilized Abstraction |

### 2.2 Implementation Smells (from DesigniteJava)

| Class | Method | Smell |
|-------|--------|-------|
| `Schedule` | `scheduleFlight` | Long Statement |
| `Schedule` | `removeFlight` | Complex Conditional, Long Statement |
| `ScheduledFlight` | constructor | Long Parameter List (×2) |
| `ScheduledFlight` | `getCrewMemberCapacity` | Magic Number |
| `ScheduledFlight` | `getCapacity` | Magic Number |
| `FlightOrder` | `processOrderWithPayPal` | Complex Conditional |
| `CreditCard` | constructor | Magic Number |
| `Helicopter` | constructor | Magic Number (×2) |
| `PassengerPlane` | constructor | Magic Number (×8), Missing Default |

### 2.3 Key Problems Summary

1. **No common Aircraft interface** → aircraft stored as `Object`, requires `instanceof` chains
2. **Payment logic hardcoded in `FlightOrder`** → violates Single Responsibility & Open/Closed Principles
3. **Duplicated validation** → `isOrderValid()` exists in both `Customer` and `FlightOrder`
4. **Public fields** → `PassengerPlane` has `public` fields (Deficient Encapsulation)
5. **Magic numbers** → hardcoded capacity values (4, 2, 500, 320, etc.) scattered across classes
6. **Complex multi-step object creation** → order construction involves many setter calls

---

## 3. Code Quality Metrics — Before (Baseline)

### 3.1 Type-Level Metrics (DesigniteJava)

| Class | NOF | NOM | LOC | WMC | DIT | LCOM | FANIN | FANOUT |
|-------|-----|-----|-----|-----|-----|------|-------|--------|
| Airport | 5 | 8 | 37 | 8 | 0 | 0.25 | 2 | 0 |
| Customer | 3 | 9 | 57 | 10 | 0 | 0.00 | 2 | 1 |
| Passenger | 1 | 2 | 9 | 2 | 0 | 0.00 | 0 | 0 |
| Flight | 4 | 8 | 52 | 12 | 0 | 0.00 | 1 | 1 |
| Schedule | 1 | 7 | 31 | 9 | 0 | 0.00 | 1 | 2 |
| **ScheduledFlight** | **3** | **11** | **61** | **17** | **1** | **0.18** | **1** | **1** |
| **FlightOrder** | **2** | **10** | **86** | **19** | **1** | **0.30** | **1** | **3** |
| Order | 5 | 10 | 37 | 10 | 0 | 0.50 | 0 | 1 |
| CreditCard | 5 | 5 | 29 | 5 | 0 | 0.00 | 1 | 0 |
| Paypal | 1 | 0 | 7 | 0 | 0 | -1.0 | 1 | 0 |
| Helicopter | 2 | 3 | 22 | 5 | 0 | 0.00 | 0 | 0 |
| PassengerDrone | 1 | 1 | 11 | 2 | 0 | 0.00 | 0 | 0 |
| PassengerPlane | 3 | 1 | 28 | 5 | 0 | 0.00 | 0 | 0 |

> **Key columns:** NOF = Number of Fields, NOM = Number of Methods, LOC = Lines of Code, WMC = Weighted Methods per Class (sum of cyclomatic complexity), DIT = Depth of Inheritance Tree, LCOM = Lack of Cohesion of Methods, FANIN = incoming dependencies, FANOUT = outgoing dependencies.

**Observations:**
- `FlightOrder` has the **highest LOC (86)** and **WMC (19)** — it does too much (order + payment)
- `ScheduledFlight` has **WMC=17** — all the `instanceof` chains add complexity
- `FlightOrder` has **LCOM=0.30** — low cohesion, methods aren't working on the same data
- `Order` has **LCOM=0.50** — half the methods don't share fields

### 3.2 Method-Level Metrics — High Complexity Methods

| Class | Method | LOC | Cyclomatic Complexity | Parameters |
|-------|--------|-----|----------------------|------------|
| `PassengerPlane` | constructor | 23 | **5** | 1 |
| `Flight` | `isAircraftValid` | 19 | **4** | 1 |
| `ScheduledFlight` | `getCapacity` | 12 | **4** | 0 |
| `ScheduledFlight` | `getCrewMemberCapacity` | 12 | **4** | 0 |
| `FlightOrder` | `processOrderWithCreditCard` | 13 | **4** | 1 |
| `FlightOrder` | `processOrderWithPayPal` | 13 | **4** | 2 |
| `Customer` | `isOrderValid` | 16 | 1 | 2 |
| `FlightOrder` | `isOrderValid` | 16 | 1 | 3 |
| `FlightOrder` | `payWithCreditCard` | 15 | **3** | 2 |
| `Helicopter` | constructor | 12 | **3** | 1 |

> Methods with CC ≥ 3 are highlighted. These are prime candidates for refactoring.

### 3.3 Checkstyle Summary

Checkstyle (Google style) reported numerous violations:
- Missing Javadoc comments on classes and public methods
- Indentation inconsistencies
- Line length exceeds 100 characters
- Member visibility issues (public fields in `PassengerPlane`)

---

## 4. Design Patterns Identified

### 4.1 Adapter Pattern

**Problem:** `PassengerPlane`, `Helicopter`, and `PassengerDrone` have **no common interface**. They store model name and capacity differently:
- `PassengerPlane`: public fields `model`, `passengerCapacity`, `crewCapacity`
- `Helicopter`: private fields with getters `getModel()`, `getPassengerCapacity()`
- `PassengerDrone`: only `model` field, **no capacity** (hardcoded as `4` in `ScheduledFlight`)

This forces `instanceof` chains in `Flight.isAircraftValid()`, `ScheduledFlight.getCapacity()`, and `ScheduledFlight.getCrewMemberCapacity()`.

**Before (Current Code):**
```java
// Flight.java — instanceof chain
protected Object aircraft;  // stored as Object!

private boolean isAircraftValid(Airport airport) {
    return Arrays.stream(airport.getAllowedAircrafts()).anyMatch(x -> {
        String model;
        if (this.aircraft instanceof PassengerPlane) {
            model = ((PassengerPlane) this.aircraft).model;
        } else if (this.aircraft instanceof Helicopter) {
            model = ((Helicopter) this.aircraft).getModel();
        } else if (this.aircraft instanceof PassengerDrone) {
            model = "HypaHype";  // hardcoded!
        } else {
            throw new IllegalArgumentException("Aircraft is not recognized");
        }
        return x.equals(model);
    });
}

// ScheduledFlight.java — another instanceof chain
public int getCapacity() throws NoSuchFieldException {
    if (this.aircraft instanceof PassengerPlane)
        return ((PassengerPlane) this.aircraft).passengerCapacity;
    if (this.aircraft instanceof Helicopter)
        return ((Helicopter) this.aircraft).getPassengerCapacity();
    if (this.aircraft instanceof PassengerDrone)
        return 4;  // magic number!
    throw new NoSuchFieldException("...");
}
```

**After (With Adapter):**
```java
// Common interface
public interface Aircraft {
    String getModel();
    int getPassengerCapacity();
    int getCrewCapacity();
}

// PassengerPlane implements Aircraft
public class PassengerPlane implements Aircraft {
    private String model;
    private int passengerCapacity;
    private int crewCapacity;

    @Override
    public String getModel() { return model; }
    @Override
    public int getPassengerCapacity() { return passengerCapacity; }
    @Override
    public int getCrewCapacity() { return crewCapacity; }
}

// Helicopter implements Aircraft
public class Helicopter implements Aircraft {
    @Override
    public int getCrewCapacity() { return 2; }
    // ... getModel(), getPassengerCapacity() already exist
}

// PassengerDrone implements Aircraft
public class PassengerDrone implements Aircraft {
    @Override
    public int getPassengerCapacity() { return 4; }
    @Override
    public int getCrewCapacity() { return 0; }
    @Override
    public String getModel() { return model; }
}

// Flight.java — CLEAN, no instanceof
private Aircraft aircraft;  // typed!

private boolean isAircraftValid(Airport airport) {
    return Arrays.asList(airport.getAllowedAircrafts()).contains(aircraft.getModel());
}

// ScheduledFlight.java — CLEAN
public int getCapacity() {
    return aircraft.getPassengerCapacity();  // one line!
}
```

**Before Class Diagram:**
```
┌─────────────────┐     uses (as Object)     ┌──────────────┐
│     Flight      │ ─────────────────────────>│    Object    │
│─────────────────│                           └──────────────┘
│ + aircraft: Object                              ▲ ▲ ▲
│ + isAircraftValid() ── instanceof ──>  ┌────────┘ │ └────────┐
│                 │                      │          │          │
│                 │              ┌───────┴──┐ ┌────┴─────┐ ┌──┴──────────┐
└─────────────────┘              │Passenger │ │Helicopter│ │PassengerDrone│
                                 │Plane     │ │          │ │             │
                                 │pub model │ │-model    │ │-model       │
                                 │pub cap   │ │-cap      │ │(no cap!)    │
                                 └──────────┘ └──────────┘ └─────────────┘
```

**After Class Diagram:**
```
                            ┌──────────────────┐
                            │   «interface»    │
                            │    Aircraft      │
                            │──────────────────│
                            │+getModel()       │
                            │+getPassengerCap()│
                            │+getCrewCapacity()│
                            └────────┬─────────┘
                        ┌────────────┼────────────────┐
                        ▼            ▼                ▼
                ┌───────────┐ ┌────────────┐ ┌───────────────┐
                │Passenger  │ │ Helicopter │ │PassengerDrone │
                │Plane      │ │            │ │               │
                └───────────┘ └────────────┘ └───────────────┘

┌─────────────────┐     uses (typed)
│     Flight      │ ──────────────────> Aircraft
│─────────────────│
│ + aircraft: Aircraft
│ + isAircraftValid() ── aircraft.getModel() (no instanceof!)
└─────────────────┘
```

**Benefits:**
- Eliminates all `instanceof` checks (3 locations)
- Type safety — `Object` replaced with `Aircraft`
- Open/Closed Principle — new aircraft types just implement `Aircraft`
- Fixes "Missing Hierarchy", "Broken Hierarchy", "Deficient Encapsulation" smells

**Drawbacks:**
- Requires modifying all three existing aircraft classes
- Slight initial effort to define the interface contract

---

### 4.2 Factory Pattern

**Problem:** Aircraft are created directly using `new PassengerPlane("A380")`, `new Helicopter("H1")`, etc. in `Runner.java`. Each constructor has its own validation logic with hardcoded model-to-capacity mappings (switch/if-else).

**Before:**
```java
// Runner.java — direct instantiation with Object type
static List<Object> aircrafts = Arrays.asList(
    new PassengerPlane("A380"),
    new PassengerPlane("A350"),
    new Helicopter("H1"),
    new PassengerDrone("HypaHype")
);
```

**After:**
```java
public class AircraftFactory {
    public static Aircraft createAircraft(String type, String model) {
        switch (type.toLowerCase()) {
            case "plane":      return new PassengerPlane(model);
            case "helicopter": return new Helicopter(model);
            case "drone":      return new PassengerDrone(model);
            default:
                throw new IllegalArgumentException("Unknown aircraft type: " + type);
        }
    }
}

// Runner.java — clean, typed
static List<Aircraft> aircrafts = Arrays.asList(
    AircraftFactory.createAircraft("plane", "A380"),
    AircraftFactory.createAircraft("plane", "A350"),
    AircraftFactory.createAircraft("helicopter", "H1"),
    AircraftFactory.createAircraft("drone", "HypaHype")
);
```

**Before Class Diagram:**
```
┌──────────┐     new PassengerPlane()     ┌───────────────┐
│  Runner  │ ───────────────────────────> │ PassengerPlane│
│          │     new Helicopter()         ├───────────────┤
│          │ ───────────────────────────> │  Helicopter   │
│          │     new PassengerDrone()     ├───────────────┤
│          │ ───────────────────────────> │ PassengerDrone│
└──────────┘                             └───────────────┘
  (Client knows all concrete classes)
```

**After Class Diagram:**
```
┌──────────┐    createAircraft()    ┌─────────────────┐    creates    ┌───────────────┐
│  Runner  │ ─────────────────────> │ AircraftFactory  │ ───────────> │  «interface»  │
│ (Client) │                        └─────────────────┘              │   Aircraft    │
└──────────┘                                                         └───────┬───────┘
  (Client only knows Aircraft)                                    ┌──────────┼──────────┐
                                                                  ▼          ▼          ▼
                                                          PassengerPlane Helicopter PassengerDrone
```

**Benefits:**
- Client code decoupled from concrete aircraft classes
- Centralized creation logic — easy to add new types in one place
- Works naturally with the Adapter pattern (`Aircraft` interface)

**Drawbacks:**
- One additional class
- Simple enough that a factory may be seen as over-engineering for 3 types

---

### 4.3 Strategy Pattern

**Problem:** `FlightOrder` has hardcoded payment methods — `processOrderWithCreditCard()` and `processOrderWithPayPal()`. Adding a new payment method (UPI, Bitcoin, bank transfer) requires modifying `FlightOrder`, violating the **Open/Closed Principle**.

**Before:**
```java
// FlightOrder.java — all payment logic inside
public boolean processOrderWithCreditCard(CreditCard creditCard) {
    if (isClosed()) return true;
    if (!cardIsPresentAndValid(creditCard))
        throw new IllegalStateException("Payment information is not set or not valid.");
    boolean isPaid = payWithCreditCard(creditCard, this.getPrice());
    if (isPaid) this.setClosed();
    return isPaid;
}

public boolean processOrderWithPayPal(String email, String password) {
    if (isClosed()) return true;
    if (email == null || password == null || !email.equals(Paypal.DATA_BASE.get(password)))
        throw new IllegalStateException("Payment information is not set or not valid.");
    boolean isPaid = payWithPayPal(email, password, this.getPrice());
    if (isPaid) this.setClosed();
    return isPaid;
}

// + payWithCreditCard(), payWithPayPal() methods also inside FlightOrder
```

**After:**
```java
// Strategy interface
public interface PaymentStrategy {
    boolean validate();
    boolean pay(double amount);
}

// Concrete strategy — Credit Card
public class CreditCardPayment implements PaymentStrategy {
    private CreditCard card;

    public CreditCardPayment(CreditCard card) { this.card = card; }

    @Override
    public boolean validate() { return card != null && card.isValid(); }

    @Override
    public boolean pay(double amount) {
        System.out.println("Paying " + amount + " using Credit Card.");
        double remaining = card.getAmount() - amount;
        if (remaining < 0) throw new IllegalStateException("Card limit reached");
        card.setAmount(remaining);
        return true;
    }
}

// Concrete strategy — PayPal
public class PayPalPayment implements PaymentStrategy {
    private String email, password;

    public PayPalPayment(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public boolean validate() {
        return email != null && password != null
            && email.equals(Paypal.DATA_BASE.get(password));
    }

    @Override
    public boolean pay(double amount) {
        System.out.println("Paying " + amount + " using PayPal.");
        return true;
    }
}

// FlightOrder — simplified
public boolean processPayment(PaymentStrategy strategy) {
    if (isClosed()) return true;
    if (!strategy.validate())
        throw new IllegalStateException("Payment information is not set or not valid.");
    boolean isPaid = strategy.pay(this.getPrice());
    if (isPaid) this.setClosed();
    return isPaid;
}
```

**Before Class Diagram:**
```
┌─────────────────────────────────┐
│          FlightOrder            │
│─────────────────────────────────│
│ + processOrderWithCreditCard()  │──── knows ──> CreditCard
│ + processOrderWithPayPal()      │──── knows ──> Paypal
│ + payWithCreditCard()           │
│ + payWithPayPal()               │
│ + cardIsPresentAndValid()       │
└─────────────────────────────────┘
```

**After Class Diagram:**
```
                ┌──────────────────────┐
                │    «interface»       │
                │   PaymentStrategy    │
                │──────────────────────│
                │ + validate(): boolean│
                │ + pay(amount): boolean│
                └──────────┬───────────┘
                     ┌─────┴──────┐
                     ▼            ▼
         ┌────────────────┐  ┌──────────────┐
         │CreditCardPayment│  │PayPalPayment │
         └────────────────┘  └──────────────┘

┌────────────────────────────┐       uses
│       FlightOrder          │ ──────────────> PaymentStrategy
│────────────────────────────│
│ + processPayment(strategy) │   (only 1 method, no payment details)
└────────────────────────────┘
```

**Benefits:**
- `FlightOrder` reduced from 10 methods / 86 LOC to ~6 methods / ~40 LOC
- Open/Closed — new payment = new class, no modification to `FlightOrder`
- Each payment strategy is independently testable
- Fixes "Complex Conditional" smell in `processOrderWithPayPal`

**Drawbacks:**
- 3 new classes (interface + 2 implementations)
- Existing test code needs updating to use the new API

---

### 4.4 Builder Pattern

**Problem:** Order creation in `Customer.createOrder()` involves many sequential steps:
1. Validate the order
2. Create `FlightOrder`
3. Set customer
4. Set price
5. Create `Passenger` objects from names
6. Set passengers on order
7. Add passengers to all scheduled flights
8. Add order to customer's list

This is a 13-line method with complex multi-step construction.

**Before:**
```java
// Customer.java
public FlightOrder createOrder(List<String> passengerNames, List<ScheduledFlight> flights, double price) {
    if (!isOrderValid(passengerNames, flights))
        throw new IllegalStateException("Order is not valid");
    FlightOrder order = new FlightOrder(flights);
    order.setCustomer(this);
    order.setPrice(price);
    List<Passenger> passengers = passengerNames.stream()
        .map(Passenger::new).collect(Collectors.toList());
    order.setPassengers(passengers);
    order.getScheduledFlights().forEach(sf -> sf.addPassengers(passengers));
    orders.add(order);
    return order;
}
```

**After:**
```java
public class FlightOrderBuilder {
    private Customer customer;
    private List<ScheduledFlight> flights;
    private List<String> passengerNames;
    private double price;

    public FlightOrderBuilder forCustomer(Customer c) { this.customer = c; return this; }
    public FlightOrderBuilder onFlights(List<ScheduledFlight> f) { this.flights = f; return this; }
    public FlightOrderBuilder withPassengers(List<String> names) { this.passengerNames = names; return this; }
    public FlightOrderBuilder atPrice(double p) { this.price = p; return this; }

    public FlightOrder build() {
        // validation, construction, passenger assignment all encapsulated
        FlightOrder order = new FlightOrder(flights);
        order.setCustomer(customer);
        order.setPrice(price);
        List<Passenger> passengers = passengerNames.stream()
            .map(Passenger::new).collect(Collectors.toList());
        order.setPassengers(passengers);
        order.getScheduledFlights().forEach(sf -> sf.addPassengers(passengers));
        return order;
    }
}

// Usage — readable, fluent API
FlightOrder order = new FlightOrderBuilder()
    .forCustomer(customer)
    .onFlights(Arrays.asList(scheduledFlight))
    .withPassengers(Arrays.asList("Amanda", "Max"))
    .atPrice(180)
    .build();
```

**Benefits:**
- Readable fluent API for order construction
- Separation of validation from construction
- Easy to add optional parameters (e.g., discount code, insurance)
- `Customer.createOrder()` becomes a thin wrapper

**Drawbacks:**
- Additional builder class
- May be overkill if order construction stays simple

---

### 4.5 Observer Pattern

**Problem:** When a booking is created or a flight schedule changes, there's **no notification mechanism**. Interested parties (customer confirmation, schedule manager, capacity tracker) are not notified.

**Before:**
```java
// ScheduledFlight.java — add passengers silently
public void addPassengers(List<Passenger> passengers) {
    this.passengers.addAll(passengers);
    // nothing else happens — no notification
}
```

**After:**
```java
public interface BookingObserver {
    void onBookingCreated(FlightOrder order);
    void onBookingCancelled(FlightOrder order);
    void onCapacityChanged(ScheduledFlight flight, int remaining);
}

// ScheduledFlight becomes a subject
public class ScheduledFlight extends Flight {
    private List<BookingObserver> observers = new ArrayList<>();

    public void addObserver(BookingObserver obs) { observers.add(obs); }
    public void removeObserver(BookingObserver obs) { observers.remove(obs); }

    public void addPassengers(List<Passenger> passengers) {
        this.passengers.addAll(passengers);
        notifyCapacityChanged();
    }

    private void notifyCapacityChanged() {
        observers.forEach(o -> {
            try { o.onCapacityChanged(this, getAvailableCapacity()); }
            catch (NoSuchFieldException e) { /* handle */ }
        });
    }
}

// Example observer
public class EmailNotificationObserver implements BookingObserver {
    @Override
    public void onBookingCreated(FlightOrder order) {
        System.out.println("Email sent to " + order.getCustomer().getEmail());
    }
    // ...
}
```

**Benefits:**
- Loose coupling — booking and notification decoupled
- Easy to add new observers (SMS, logging, analytics) without modifying ScheduledFlight
- Supports event-driven architecture

**Drawbacks:**
- Additional complexity for a prototype system
- Must manage observer lifecycle (add/remove)
- Potential for memory leaks if observers aren't cleaned up

---

### 4.6 Chain of Responsibility Pattern

**Problem:** Order validation logic is **duplicated** in both `Customer.isOrderValid()` and `FlightOrder.isOrderValid()`. Both check the same things:
1. Customer not on no-fly list
2. Passengers not on no-fly list
3. Capacity available on flights

These are independent checks mixed into one long boolean expression.

**Before:**
```java
// Customer.java — validation
private boolean isOrderValid(List<String> passengerNames, List<ScheduledFlight> flights) {
    boolean valid = true;
    valid = valid && !FlightOrder.getNoFlyList().contains(this.getName());
    valid = valid && passengerNames.stream().noneMatch(p -> FlightOrder.getNoFlyList().contains(p));
    valid = valid && flights.stream().allMatch(sf -> {
        try { return sf.getAvailableCapacity() >= passengerNames.size(); }
        catch (NoSuchFieldException e) { return false; }
    });
    return valid;
}

// FlightOrder.java — SAME logic duplicated!
private boolean isOrderValid(Customer customer, List<String> passengerNames, List<ScheduledFlight> flights) {
    // exact same checks...
}
```

**After:**
```java
public abstract class ValidationHandler {
    private ValidationHandler next;

    public ValidationHandler setNext(ValidationHandler handler) {
        this.next = handler;
        return handler;
    }

    public boolean validate(Customer customer, List<String> passengers, List<ScheduledFlight> flights) {
        if (next != null) return next.validate(customer, passengers, flights);
        return true; // end of chain — all passed
    }
}

public class NoFlyListCheck extends ValidationHandler {
    @Override
    public boolean validate(Customer customer, List<String> passengers, List<ScheduledFlight> flights) {
        if (FlightOrder.getNoFlyList().contains(customer.getName())) return false;
        if (passengers.stream().anyMatch(p -> FlightOrder.getNoFlyList().contains(p))) return false;
        return super.validate(customer, passengers, flights);
    }
}

public class CapacityCheck extends ValidationHandler {
    @Override
    public boolean validate(Customer customer, List<String> passengers, List<ScheduledFlight> flights) {
        boolean hasCapacity = flights.stream().allMatch(sf -> {
            try { return sf.getAvailableCapacity() >= passengers.size(); }
            catch (NoSuchFieldException e) { return false; }
        });
        if (!hasCapacity) return false;
        return super.validate(customer, passengers, flights);
    }
}

// Usage — single validation chain, no duplication
ValidationHandler chain = new NoFlyListCheck();
chain.setNext(new CapacityCheck());
boolean isValid = chain.validate(customer, passengerNames, flights);
```

**Benefits:**
- Eliminates duplicated validation code (DRY principle)
- Each validation rule is a single class (SRP)
- Easy to add/remove/reorder validation steps
- E.g., add `PaymentVerificationCheck`, `AgeRestrictionCheck` without touching existing code

**Drawbacks:**
- More classes for simple validations
- Chain ordering matters — must be set up correctly
- Slightly harder to debug than a simple boolean method

---

## 5. Summary & Next Steps

### Patterns × Code Smells Mapping

| Pattern | Fixes These Smells |
|---------|-------------------|
| **Adapter** | Missing Hierarchy, Broken Hierarchy, Deficient Encapsulation, `instanceof` chains |
| **Factory** | Unutilized Abstraction, tight coupling in aircraft creation |
| **Strategy** | Complex Conditional in payment, SRP violation in FlightOrder |
| **Builder** | Long Parameter List, complex multi-step construction |
| **Observer** | Tight coupling between booking and notification |
| **Chain of Responsibility** | Duplicated validation, scattered validation logic |

### Expected Metric Improvements (After Applying All Patterns)

| Metric | Before | Expected After | Why |
|--------|--------|---------------|-----|
| FlightOrder WMC | 19 | ~8 | Payment methods extracted to strategies |
| FlightOrder LOC | 86 | ~35 | Payment code moved out |
| ScheduledFlight WMC | 17 | ~10 | instanceof chains removed |
| Flight CC (isAircraftValid) | 4 | 1 | Polymorphism replaces instanceof |
| ScheduledFlight CC (getCapacity) | 4 | 1 | Direct interface call |
| Code Duplication | isOrderValid ×2 | ×0 | Single validation chain |
| Design Smells Count | 15 | ~5 | Hierarchy, encapsulation, coupling fixed |

### Version 2 (Full Report) Will Add:
- Actual code implementation (all patterns applied to codebase)
- Actual "After" metrics from DesigniteJava/Checkstyle re-run
- Side-by-side metrics comparison table
- Updated test cases
- Detailed analysis of metric changes
