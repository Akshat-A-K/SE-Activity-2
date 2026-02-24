# SE Activity 2 — Codebase Analysis & Design Patterns Plan

## 1. Assignment Summary

**Course:** S26CS6.401 — Software Engineering  
**Due:** 04 Mar 2026, 11:59 PM  
**Repo:** https://github.com/Sidx-sys/Reservation-System-Starter

**What to do:**
1. Identify 6 design patterns applicable to this codebase
2. Describe each pattern and justify why it fits
3. For each pattern: show a **before** class diagram and an **after** class diagram
4. Provide **code snippets** showing the implementation
5. Discuss **benefits and drawbacks**
6. Run **code quality metrics** (static analysis) before and after applying patterns — compare in a table

**Patterns to consider (from PDF):**
- Observer Pattern
- Factory Pattern
- Adapter Pattern
- Builder Pattern
- Strategy Pattern
- Chain of Responsibility / Command Pattern

---

## 2. Codebase Explanation (File by File)

This is a **Flight Reservation System** — customers can book passengers on scheduled flights and pay via credit card or PayPal.

### 2.1 `Runner.java` (Entry Point)

```java
// Creates sample airports, aircraft, flights, and a schedule
static List<Airport> airports = Arrays.asList(
    new Airport("Berlin Airport", "BER", "Berlin, Berlin"),
    ...
);
static List<Object> aircrafts = Arrays.asList(   // <-- stored as Object!
    new PassengerPlane("A380"),
    new Helicopter("H1"),
    new PassengerDrone("HypaHype")
);
static List<Flight> flights = Arrays.asList(
    new Flight(1, airports.get(0), airports.get(1), aircrafts.get(0)),
    ...
);
```

**Problem:** Aircraft list is `List<Object>` because there's no common interface.

---

### 2.2 `Airport.java`

```java
public class Airport {
    private final String name;
    private final String code;
    private final String location;
    private List<Flight> flights;
    private String[] allowedAircrafts;  // checks aircraft by model name string
}
```

- Stores airport info and a whitelist of allowed aircraft model names.
- Validation is done by comparing **string names** — fragile and error-prone.

---

### 2.3 `Passenger.java`

```java
public class Passenger {
    private final String name;
}
```

- Simple value class. No issues here.

---

### 2.4 `Customer.java`

```java
public class Customer {
    private String email, name;
    private List<Order> orders;

    public FlightOrder createOrder(List<String> passengerNames, List<ScheduledFlight> flights, double price) {
        if (!isOrderValid(...)) throw new IllegalStateException("Order is not valid");
        FlightOrder order = new FlightOrder(flights);
        order.setCustomer(this);
        order.setPrice(price);
        // ... create passengers, add to flights
        return order;
    }

    private boolean isOrderValid(...) {
        // checks no-fly list, capacity — DUPLICATED in FlightOrder too!
    }
}
```

**Problems:**
- `isOrderValid()` is **duplicated** in both `Customer` and `FlightOrder`
- Order creation is a multi-step process with many `set` calls — candidate for **Builder**

---

### 2.5 Aircraft Classes (NO common interface!)

#### `PassengerPlane.java`
```java
public class PassengerPlane {
    public String model;            // PUBLIC field!
    public int passengerCapacity;   // PUBLIC field!
    public int crewCapacity;        // PUBLIC field!

    public PassengerPlane(String model) {
        switch (model) {  // hardcoded switch
            case "A380": passengerCapacity = 500; crewCapacity = 42; break;
            case "A350": passengerCapacity = 320; crewCapacity = 40; break;
            ...
        }
    }
}
```

#### `Helicopter.java`
```java
public class Helicopter {
    private final String model;              // private with getter
    private final int passengerCapacity;

    public String getModel() { return model; }
    public int getPassengerCapacity() { return passengerCapacity; }
}
```

#### `PassengerDrone.java`
```java
public class PassengerDrone {
    private final String model;
    // NO capacity field at all! Hardcoded as 4 elsewhere in ScheduledFlight
}
```

**Problems:**
- **No common interface** — all three are completely unrelated classes
- Inconsistent access: `PassengerPlane` has public fields, `Helicopter` has getters, `PassengerDrone` has nothing
- Capacity for drone is hardcoded as `4` inside `ScheduledFlight` — not in the drone class itself
- Forces `instanceof` checks everywhere they're used

---

### 2.6 `Flight.java`

```java
public class Flight {
    private int number;
    private Airport departure, arrival;
    protected Object aircraft;   // <-- Object type!

    private boolean isAircraftValid(Airport airport) {
        return Arrays.stream(airport.getAllowedAircrafts()).anyMatch(x -> {
            String model;
            if (this.aircraft instanceof PassengerPlane) {          // instanceof chain!
                model = ((PassengerPlane) this.aircraft).model;
            } else if (this.aircraft instanceof Helicopter) {
                model = ((Helicopter) this.aircraft).getModel();
            } else if (this.aircraft instanceof PassengerDrone) {
                model = "HypaHype";                                 // hardcoded!
            } else {
                throw new IllegalArgumentException("Aircraft is not recognized");
            }
            return x.equals(model);
        });
    }
}
```

**Problems:**
- Aircraft stored as `Object` — no type safety
- Ugly `instanceof` chain to extract model name
- Every new aircraft type requires modifying this method (violates **Open/Closed Principle**)

---

### 2.7 `ScheduledFlight.java` (extends Flight)

```java
public class ScheduledFlight extends Flight {
    private final List<Passenger> passengers;
    private final Date departureTime;
    private double currentPrice = 100;

    public int getCapacity() throws NoSuchFieldException {
        if (this.aircraft instanceof PassengerPlane) {         // instanceof again!
            return ((PassengerPlane) this.aircraft).passengerCapacity;
        }
        if (this.aircraft instanceof Helicopter) {
            return ((Helicopter) this.aircraft).getPassengerCapacity();
        }
        if (this.aircraft instanceof PassengerDrone) {
            return 4;                                           // magic number!
        }
        throw new NoSuchFieldException("...");
    }

    public int getCrewMemberCapacity() throws NoSuchFieldException {
        // same instanceof chain again for crew capacity
    }
}
```

**Problems:**
- `instanceof` chains repeated for **every** method that needs aircraft info
- Magic number `4` for drone capacity, `2` for helicopter crew
- Adding a new aircraft type means editing multiple methods

---

### 2.8 `Schedule.java`

```java
public class Schedule {
    private List<ScheduledFlight> scheduledFlights;

    public void scheduleFlight(Flight flight, Date date) { ... }
    public void removeFlight(Flight flight) { ... }
    public ScheduledFlight searchScheduledFlight(int flightNumber) { ... }
}
```

- Simple manager class. Relatively clean.

---

### 2.9 `Order.java` (Base class)

```java
public class Order {
    private final UUID id;
    private double price;
    private boolean isClosed = false;
    private Customer customer;
    private List<Passenger> passengers;
    // getters and setters
}
```

- Base order class. Fine structurally.

---

### 2.10 `FlightOrder.java` (extends Order) — MOST PROBLEMATIC FILE

```java
public class FlightOrder extends Order {
    private final List<ScheduledFlight> flights;
    static List<String> noFlyList = Arrays.asList("Peter", "Johannes");

    // DUPLICATED validation logic (also in Customer.java)
    private boolean isOrderValid(Customer customer, List<String> passengerNames, ...) { ... }

    // ALL payment logic hardcoded here:
    public boolean processOrderWithCreditCard(CreditCard creditCard) { ... }
    public boolean processOrderWithPayPal(String email, String password) { ... }
    public boolean payWithCreditCard(CreditCard card, double amount) { ... }
    public boolean payWithPayPal(String email, String password, double amount) { ... }

    // Also takes raw credit card details:
    public boolean processOrderWithCreditCardDetail(String number, Date expirationDate, String cvv) { ... }
}
```

**Problems:**
- **Violates Single Responsibility Principle** — handles order management AND payment processing
- **Violates Open/Closed Principle** — adding a new payment method (e.g., UPI, Bitcoin) requires modifying this class
- `isOrderValid()` duplicated from `Customer`
- Payment logic is tightly coupled to `FlightOrder`

---

### 2.11 Payment Classes

#### `CreditCard.java`
```java
public class CreditCard {
    private double amount;
    private String number;
    private Date date;
    private String cvv;
    private boolean valid;
    // dummy validation
}
```

#### `Paypal.java`
```java
public class Paypal {
    public static final Map<String, String> DATA_BASE = new HashMap<>();
    static {
        DATA_BASE.put("amanda1985", "amanda@ya.com");
        DATA_BASE.put("qwerty", "john@amazon.eu");
    }
}
```

- `Paypal` is just a static map — no behavior, no interface.

---

## 3. Code Smells Summary

| # | Smell | Location | Severity |
|---|-------|----------|----------|
| 1 | Aircraft stored as `Object` — no common type | `Flight.java`, `Runner.java` | **High** |
| 2 | `instanceof` chains repeated 3+ times | `Flight.java`, `ScheduledFlight.java` | **High** |
| 3 | Payment logic hardcoded in `FlightOrder` | `FlightOrder.java` | **High** |
| 4 | Duplicated `isOrderValid()` method | `Customer.java` & `FlightOrder.java` | **Medium** |
| 5 | No encapsulation — public fields in `PassengerPlane` | `PassengerPlane.java` | **Medium** |
| 6 | Magic numbers (drone capacity=4, helicopter crew=2) | `ScheduledFlight.java` | **Medium** |
| 7 | Multi-step order creation with many setters | `Customer.createOrder()` | **Low-Medium** |
| 8 | No notification mechanism for booking events | Entire system | **Low** |
| 9 | Validation scattered across classes | `Customer`, `FlightOrder` | **Medium** |

---

## 4. Design Patterns to Apply

### Pattern 1: ADAPTER PATTERN — Common Aircraft Interface

**Where:** `PassengerPlane`, `Helicopter`, `PassengerDrone`

**Why:** The three aircraft classes have no common interface. They store model/capacity differently. This forces `instanceof` checks in `Flight.java` and `ScheduledFlight.java`.

**Solution:** Create an `Aircraft` interface with common methods:
```java
public interface Aircraft {
    String getModel();
    int getPassengerCapacity();
    int getCrewCapacity();
}
```
Make all three classes implement it (or use adapter wrappers). This eliminates ALL `instanceof` chains.

**Before:** `Flight` stores aircraft as `Object`, uses `instanceof` to extract model.  
**After:** `Flight` stores aircraft as `Aircraft`, calls `aircraft.getModel()` directly.

**Benefits:** Eliminates instanceof, enables polymorphism, type safety  
**Drawbacks:** Requires modifying existing aircraft classes (or adding adapters)

---

### Pattern 2: FACTORY PATTERN — Aircraft Creation

**Where:** Aircraft instantiation in `Runner.java`

**Why:** Aircraft creation uses direct `new` calls with string-based model names. The logic for what capacity each model has is inside each class's constructor with hardcoded switch/if-else.

**Solution:** Create an `AircraftFactory`:
```java
public class AircraftFactory {
    public static Aircraft createAircraft(String type, String model) {
        switch (type) {
            case "plane":      return new PassengerPlane(model);
            case "helicopter": return new Helicopter(model);
            case "drone":      return new PassengerDrone(model);
            default: throw new IllegalArgumentException("Unknown aircraft type");
        }
    }
}
```

**Benefits:** Centralized creation logic, easy to add new aircraft types, decouples client from concrete classes  
**Drawbacks:** One more class to maintain

---

### Pattern 3: STRATEGY PATTERN — Payment Processing

**Where:** `FlightOrder.java` — methods `processOrderWithCreditCard()`, `processOrderWithPayPal()`

**Why:** Payment logic is hardcoded inside `FlightOrder`. Adding a new payment method (UPI, Bitcoin, bank transfer) means modifying `FlightOrder`. This violates Open/Closed Principle.

**Solution:** Create a `PaymentStrategy` interface:
```java
public interface PaymentStrategy {
    boolean pay(double amount);
    boolean validate();
}

public class CreditCardPayment implements PaymentStrategy {
    private CreditCard card;
    public boolean pay(double amount) { /* credit card logic */ }
    public boolean validate() { return card != null && card.isValid(); }
}

public class PayPalPayment implements PaymentStrategy {
    private String email, password;
    public boolean pay(double amount) { /* paypal logic */ }
    public boolean validate() { /* check paypal DB */ }
}
```

Then `FlightOrder` just does:
```java
public boolean processPayment(PaymentStrategy strategy) {
    if (isClosed()) return true;
    if (!strategy.validate()) throw new IllegalStateException("Invalid payment");
    boolean isPaid = strategy.pay(this.getPrice());
    if (isPaid) this.setClosed();
    return isPaid;
}
```

**Benefits:** Open/Closed Principle — new payment methods need only a new class, not modifying FlightOrder  
**Drawbacks:** More classes; slight overhead for simple cases

---

### Pattern 4: BUILDER PATTERN — Order Construction

**Where:** `Customer.createOrder()` method

**Why:** Creating an order involves many steps:
1. Validate the order
2. Create `FlightOrder` object
3. Set customer
4. Set price
5. Create passenger objects from names
6. Set passengers on order
7. Add passengers to scheduled flights
8. Add order to customer's list

This is a complex multi-step construction process with many setter calls.

**Solution:** Create a `FlightOrderBuilder`:
```java
public class FlightOrderBuilder {
    private Customer customer;
    private List<ScheduledFlight> flights;
    private List<String> passengerNames;
    private double price;

    public FlightOrderBuilder setCustomer(Customer c) { this.customer = c; return this; }
    public FlightOrderBuilder setFlights(List<ScheduledFlight> f) { this.flights = f; return this; }
    public FlightOrderBuilder setPassengerNames(List<String> names) { this.passengerNames = names; return this; }
    public FlightOrderBuilder setPrice(double p) { this.price = p; return this; }

    public FlightOrder build() {
        validate();
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
```

**Benefits:** Cleaner construction, separation of validation from creation, readable API  
**Drawbacks:** Additional builder class; may be overkill for simple orders

---

### Pattern 5: OBSERVER PATTERN — Booking Notifications

**Where:** When passengers are booked on flights

**Why:** Currently, when a booking happens, there's no notification mechanism. In a real system, you'd want to notify:
- The customer (confirmation email)
- The airline (capacity update)
- The schedule manager (flight status update)

**Solution:**
```java
public interface BookingObserver {
    void onBookingCreated(FlightOrder order);
    void onBookingCancelled(FlightOrder order);
}

public class ScheduledFlight extends Flight {
    private List<BookingObserver> observers = new ArrayList<>();

    public void addObserver(BookingObserver observer) {
        observers.add(observer);
    }

    public void notifyObservers(FlightOrder order) {
        observers.forEach(o -> o.onBookingCreated(order));
    }
}
```

**Benefits:** Loose coupling between booking and notification, easy to add new observers  
**Drawbacks:** Complexity; observers must be managed (added/removed); potential memory leaks if not cleaned up

---

### Pattern 6: CHAIN OF RESPONSIBILITY PATTERN — Order Validation

**Where:** `isOrderValid()` in `Customer.java` and `FlightOrder.java`

**Why:** Validation logic is duplicated and scattered. There are multiple checks:
1. Customer not on no-fly list
2. All passengers not on no-fly list
3. Sufficient capacity on all flights

These are independent validation steps that could be chained.

**Solution:**
```java
public abstract class OrderValidationHandler {
    private OrderValidationHandler next;

    public OrderValidationHandler setNext(OrderValidationHandler handler) {
        this.next = handler;
        return handler;
    }

    public boolean validate(Customer customer, List<String> passengers, List<ScheduledFlight> flights) {
        if (next != null) return next.validate(customer, passengers, flights);
        return true;
    }
}

public class NoFlyListValidation extends OrderValidationHandler {
    public boolean validate(Customer customer, List<String> passengers, List<ScheduledFlight> flights) {
        if (FlightOrder.getNoFlyList().contains(customer.getName())) return false;
        if (passengers.stream().anyMatch(p -> FlightOrder.getNoFlyList().contains(p))) return false;
        return super.validate(customer, passengers, flights);
    }
}

public class CapacityValidation extends OrderValidationHandler {
    public boolean validate(Customer customer, List<String> passengers, List<ScheduledFlight> flights) {
        boolean hasCapacity = flights.stream().allMatch(sf -> {
            try { return sf.getAvailableCapacity() >= passengers.size(); }
            catch (NoSuchFieldException e) { return false; }
        });
        if (!hasCapacity) return false;
        return super.validate(customer, passengers, flights);
    }
}
```

Usage:
```java
OrderValidationHandler chain = new NoFlyListValidation();
chain.setNext(new CapacityValidation());
boolean isValid = chain.validate(customer, passengerNames, flights);
```

**Benefits:** Eliminates duplication, each validation is a single class (SRP), easy to add/remove/reorder checks  
**Drawbacks:** More classes, slightly harder to debug the chain

---

## 5. Code Quality Metrics (What to Measure)

Use a tool like **PMD**, **Checkstyle**, **SonarQube**, or **JaCoCo**. Measure these before and after:

| Metric | What It Measures |
|--------|-----------------|
| Cyclomatic Complexity | Number of decision paths in methods |
| Lines of Code (LOC) | Total/per-method code size |
| Coupling Between Objects (CBO) | How tightly classes depend on each other |
| Depth of Inheritance Tree (DIT) | Class hierarchy depth |
| Number of Methods (NOM) | Methods per class |
| Lack of Cohesion of Methods (LCOM) | How focused each class is |
| Code Duplication | Percentage of duplicated code |

**Expected improvements after refactoring:**
- Cyclomatic complexity ↓ (removing instanceof chains)
- CBO ↓ (decoupling payment from order)
- Code duplication ↓ (removing duplicate isOrderValid)
- LCOM ↓ (FlightOrder becomes more cohesive)
- LOC may ↑ slightly (more classes/interfaces) but per-class LOC ↓

---

## 6. Quick Reference — What Goes in Your Report

For each of the 6 patterns:

1. **Pattern Name & Description** (1 paragraph)
2. **Where it applies** in the codebase (specific classes/methods)
3. **Before Class Diagram** (UML showing current structure)
4. **After Class Diagram** (UML showing refactored structure)
5. **Code Snippets** (before and after)
6. **Benefits & Drawbacks** (2-3 bullet points each)

Plus:
7. **Code Quality Metrics Table** — before vs after
8. **Analysis** of metric changes
