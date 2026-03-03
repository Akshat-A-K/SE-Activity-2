# Take Home Activity -2 Report

**Course:** S26CS6.401 — Software Engineering  
**Activity:** Take Home Activity - 2 (Design Patterns Implementation)  
**Repository:** https://github.com/Sidx-sys/Reservation-System-Starter

## Team Details
- **Team No:** 27
- **Member 1:** 2025201005 Akshat
- **Member 2:** 2025201008 Om
- **Member 3:** 2025201046 Hardik
- **Member 4:** 2025201065 Gaurav
- **Member 5:** 2025201093 Parv

---

## 1. Assumptions
1. Existing functional behavior must remain unchanged (validated by test suite).
2. Design pattern implementation is incremental; compatibility methods are retained where needed.
3. Static-analysis tools and configuration used for **Before** and **After** remain the same.
4. UMLs represent structural intent and implementation-level relations in the current codebase.

---

## 2. Codebase Summary
The system is a flight reservation domain where customers create orders for passengers on scheduled flights and complete payment using credit card or PayPal.

Key packages:
- flight.reservation (entities: Airport, Customer, Passenger)
- flight.reservation.flight (core flight and schedule model)
- flight.reservation.order (order lifecycle)
- flight.reservation.payment (payment model)
- flight.reservation.plane (aircraft model)

---

## 3. Design Patterns Applied

### 3.1 Factory Pattern
**Intent:** Centralize aircraft construction and remove object creation scattering.

**Before Diagram:** `docs/class_diagrams/factory_before.puml`  
**After Diagram:** `docs/class_diagrams/factory_after.puml`

**Implementation Highlights:**
- Introduced `Aircraft` interface and `AircraftFactory`.
- Updated `Runner`, `Flight`, and `ScheduledFlight` to use typed `Aircraft`.

**Why this pattern here:**
The original flow created concrete aircraft types directly, which spread creation logic across the codebase and increased `instanceof` checks. A factory moves this decision to one place and keeps flight-domain classes focused on behavior instead of construction details.

**Key Code Locations:**
- src/main/java/flight/reservation/plane/Aircraft.java
- src/main/java/flight/reservation/plane/AircraftFactory.java
- src/main/java/Runner.java
- src/main/java/flight/reservation/flight/Flight.java
- src/main/java/flight/reservation/flight/ScheduledFlight.java

**Benefits:** Better type safety, Open/Closed alignment, reduced creation coupling.  
**Tradeoff:** Additional factory abstraction.

---

### 3.2 Adapter Pattern
**Intent:** Normalize heterogeneous payment APIs via one interface.

**Before Diagram:** `docs/class_diagrams/adapter_before.puml`  
**After Diagram:** `docs/class_diagrams/adapter_after.puml`

**Implementation Highlights:**
- Introduced `PaymentProcessor`.
- Added `CreditCardAdapter` and `PaypalAdapter`.
- Refactored `FlightOrder` payment flow to use adapter abstraction.

**Why this pattern here:**
Credit card and PayPal had different interfaces and validation rules. The order flow should not depend on these differences. Adapters wrap payment-specific behavior and expose a common contract (`pay`) to `FlightOrder`.

**Key Code Locations:**
- src/main/java/flight/reservation/payment/PaymentProcessor.java
- src/main/java/flight/reservation/payment/CreditCardAdapter.java
- src/main/java/flight/reservation/payment/PaypalAdapter.java
- src/main/java/flight/reservation/order/FlightOrder.java

**Benefits:** Payment extensibility without changing order core logic.  
**Tradeoff:** More classes.

---

### 3.3 Chain of Responsibility Pattern
**Intent:** Decompose order validation into independent handlers.

**Before Diagram:** `docs/class_diagrams/chain_before.puml`  
**After Diagram:** `docs/class_diagrams/chain_after.puml`

**Implementation Highlights:**
- Added `OrderValidationContext`.
- Added handler base `OrderValidationHandler`.
- Added concrete handlers: `NoFlyValidator`, `CapacityValidator`.
- Wired chain from `Customer.isOrderValid(...)`.

**Why this pattern here:**
Order validation combines independent checks (regulatory, capacity, and future checks). A chain executes each check in sequence and short-circuits on failure. This avoids large conditional blocks and supports adding new rules without editing old validators.

**Key Code Locations:**
- src/main/java/flight/reservation/order/validation/OrderValidationContext.java
- src/main/java/flight/reservation/order/validation/OrderValidationHandler.java
- src/main/java/flight/reservation/order/validation/NoFlyValidator.java
- src/main/java/flight/reservation/order/validation/CapacityValidator.java
- src/main/java/flight/reservation/Customer.java

**Benefits:** Removes duplicated validation logic, improves modularity.  
**Tradeoff:** Slightly more orchestration logic.

---

### 3.4 Strategy Pattern
**Intent:** Make pricing logic pluggable and testable.

**Before Diagram:** `docs/class_diagrams/strategy_before.puml`  
**After Diagram:** `docs/class_diagrams/strategy_after.puml`

**Implementation Highlights:**
- Added `PricingStrategy` interface.
- Added `FixedPricing` and `DemandPricing` strategies.
- Integrated strategy into `ScheduledFlight` (`setPricingStrategy`, `applyPricingStrategy`).

**Why this pattern here:**
Pricing is business policy and changes over time. Embedding one algorithm directly in `ScheduledFlight` would force repeated edits. Strategy isolates pricing rules and allows runtime selection of policies such as fixed fare or load-factor-based pricing.

**Key Code Locations:**
- src/main/java/flight/reservation/flight/pricing/PricingStrategy.java
- src/main/java/flight/reservation/flight/pricing/FixedPricing.java
- src/main/java/flight/reservation/flight/pricing/DemandPricing.java
- src/main/java/flight/reservation/flight/ScheduledFlight.java

**Benefits:** Supports alternate pricing policies without class modification.  
**Tradeoff:** Additional strategy lifecycle/state to manage.

---

### 3.5 Observer Pattern
**Intent:** Publish schedule/price events to decoupled listeners.

**Before Diagram:** `docs/class_diagrams/observer_before.puml`  
**After Diagram:** `docs/class_diagrams/observer_after.puml`

**Implementation Highlights:**
- Added `ScheduleObserver`, `ScheduleSubject`, `ScheduleEvent`, `ScheduleEventType`.
- `Schedule` now emits `FLIGHT_SCHEDULED` and `FLIGHT_REMOVED`.
- `ScheduledFlight` emits `PRICE_UPDATED`.

**Why this pattern here:**
Schedule operations can trigger downstream actions (notifications, analytics, logging). Observer decouples event producers (`Schedule`, `ScheduledFlight`) from consumers, enabling extension without changing scheduling logic.

**Key Code Locations:**
- src/main/java/flight/reservation/flight/observer/ScheduleObserver.java
- src/main/java/flight/reservation/flight/observer/ScheduleSubject.java
- src/main/java/flight/reservation/flight/observer/ScheduleEvent.java
- src/main/java/flight/reservation/flight/observer/ScheduleEventType.java
- src/main/java/flight/reservation/flight/Schedule.java
- src/main/java/flight/reservation/flight/ScheduledFlight.java

**Benefits:** Loose coupling for notification features.  
**Tradeoff:** Observer management overhead.

---

### 3.6 Builder Pattern
**Intent:** Simplify multi-step object construction.

**Before Diagram:** `docs/class_diagrams/builder_before.puml`  
**After Diagram:** `docs/class_diagrams/builder_after.puml`

**Implementation Highlights:**
- Added `FlightOrderBuilder` and `ScheduledFlightBuilder`.
- Refactored `Customer.createOrder(...)` to use `FlightOrderBuilder`.
- Refactored `Schedule.scheduleFlight(...)` to use `ScheduledFlightBuilder`.

**Why this pattern here:**
`FlightOrder` and `ScheduledFlight` require multiple fields and setup steps. Builder provides readable, step-by-step construction and reduces constructor complexity while preserving object validity.

**Key Code Locations:**
- src/main/java/flight/reservation/order/builder/FlightOrderBuilder.java
- src/main/java/flight/reservation/flight/builder/ScheduledFlightBuilder.java
- src/main/java/flight/reservation/Customer.java
- src/main/java/flight/reservation/flight/Schedule.java

**Benefits:** Readable construction flow, optional attributes handled cleanly.  
**Tradeoff:** Additional builder classes.

---

## 4. Code Snippets (Pattern Examples)

### 4.1 Factory Example (`AircraftFactory`)
```java
public static Aircraft createAircraft(String type, String model) {
	switch (type.toLowerCase()) {
		case "plane":
			return new PassengerPlane(model);
		case "helicopter":
			return new Helicopter(model);
		case "drone":
			return new PassengerDrone(model);
		default:
			throw new IllegalArgumentException("Unknown type");
	}
}
```

### 4.2 Adapter Example (`CreditCardAdapter`)
```java
public class CreditCardAdapter implements PaymentProcessor {
    @Override
    public boolean pay(double amount) {
        if (creditCard == null || !creditCard.isValid()) {
            throw new IllegalStateException("Invalid card");
        }
        // deduct amount and return status
        return true;
    }
}
```

### 4.3 Chain Example (`OrderValidationHandler`)
```java
public boolean validate(OrderValidationContext context) {
    if (!check(context)) {
        return false;
    }
    return next == null || next.validate(context);
}
```

### 4.4 Strategy Example (`DemandPricing`)
```java
public double compute(ScheduledFlight scheduledFlight) {
	double passengers = scheduledFlight.getPassengers().size();
	double capacity = scheduledFlight.getCapacity();
	double loadFactor = passengers / capacity;
	return scheduledFlight.getBasePrice() * (1 + (0.5 * loadFactor));
}
```

### 4.5 Observer Example (`Schedule`)
```java
public void scheduleFlight(Flight flight, Date date) {
	// build flight object
	notifyObservers(
		new ScheduleEvent(ScheduleEventType.FLIGHT_SCHEDULED, scheduledFlight)
	);
}
```

### 4.6 Builder Example (`FlightOrderBuilder`)
```java
public FlightOrder build() {
    FlightOrder order = new FlightOrder(safeFlights);
    order.setCustomer(customer);
    order.setPrice(price);
    // set passengers and attach to flights
    return order;
}
```

Main modified files by pattern:
- **Factory:** Runner, AircraftFactory, Aircraft, Flight, ScheduledFlight
- **Adapter:** FlightOrder, PaymentProcessor, CreditCardAdapter, PaypalAdapter
- **Chain:** Customer, validation handlers under order/validation
- **Strategy:** ScheduledFlight, pricing strategy classes under flight/pricing
- **Observer:** Schedule, ScheduledFlight, observer classes under flight/observer
- **Builder:** Customer, Schedule, builders under order/builder and flight/builder

---

## 5. Code Quality Metrics

### 5.1 Before (Baseline)
Baseline analysis is available in:
- analysis-output/Before.md
- analysis-output/checkstyle-report.xml
- analysis-output/designite-output/*

### 5.2 After (Current Refactored Code)
Executed with the exact same tools/configuration used in baseline:
- Checkstyle
- DesigniteJava

Generated output locations:
- analysis-output/After.md
- analysis-output/checkstyle-report-after.xml
- analysis-output/designite-output-after/*

### 5.3 Before/After Comparison Table
| Metric | Before | After | Change |
|---|---:|---:|---:|
| Total methods (NOM) | 71 | 116 | +45 |
| Total LOC | 475 | 678 | +203 |
| LOC (key classes avg)* | 58.75 | 51.00 | -7.75 |
| WMC (key classes avg)* | 13.75 | 12.75 | -1.00 |
| LCOM (key classes avg)* | 0.12 | 0.17 | +0.05 |
| FANOUT (key classes avg)* | 1.75 | 3.50 | +1.75 |

Metric notes:
- Key classes used for averages: Customer, Schedule, ScheduledFlight, FlightOrder.
- LCOM increased slightly due to responsibility split and supporting abstractions.
- FANOUT increase is expected because pattern interfaces and helper classes were added.
- Checkstyle increase is mainly from style and documentation issues in newly introduced files.

### 5.4 Per-Metric Analysis

**Total methods (NOM): 71 → 116 (+45)**
Additional methods come from the new pattern classes (builder setters, adapter `pay()`, observer `addObserver`/`removeObserver`/`notifyObservers`, strategy `compute()`, validation `check()`/`validate()`). Each method is small and focused (typically 1–5 lines), which is consistent with the Single Responsibility Principle. No bloated methods were introduced.

**Total LOC: 475 → 678 (+203)**
Total lines increased proportionally with the 17 new types. However, the LOC of the original key classes decreased — meaning complexity was redistributed from a few large classes into many small, focused ones. This is an expected and desirable effect of applying patterns.

**LOC (avg key classes): 58.75 → 51.00 (↓ improved)**
The average LOC for `Customer`, `Schedule`, `ScheduledFlight`, and `FlightOrder` decreased. `FlightOrder` shrank because validation logic was extracted to Chain handlers and payment dispatch now delegates to `PaymentProcessor` adapters. `Customer` shrank because order construction was offloaded to `FlightOrderBuilder`. `Schedule` rose slightly due to observer notification calls. `ScheduledFlight` rose because it now hosts `PricingStrategy` and observer fields. Net effect: the most complex classes became smaller.

**WMC (avg key classes): 13.75 → 12.75 (↓ improved)**
Weighted method complexity dropped because complex validation and payment methods were moved out of `FlightOrder` and `Customer` into dedicated handler and adapter classes. The improvement is modest since delegation calls still count toward WMC, but the per-method complexity within key classes is lower.

**LCOM (avg key classes): 0.12 → 0.17 (↑ slightly worsened)**
Lack of cohesion increased marginally. `ScheduledFlight` now has pricing-strategy fields and observer lists that are functionally independent of its flight-data fields, which lowers cohesion numerically. `FlightOrder` LCOM rose because adapter convenience methods coexist with the main `processOrder(PaymentProcessor)`. This is an acceptable tradeoff — the classes are more focused in behavior despite the slight metric increase.

**FANOUT (avg key classes): 1.75 → 3.50 (↑ increased)**
Fan-out doubled because key classes now depend on pattern interfaces (`PricingStrategy`, `ScheduleObserver`, `ScheduleEvent`, `OrderValidationHandler`, `FlightOrderBuilder`, etc.). This is expected when applying design patterns — coupling shifts from concrete classes to stable abstractions. From a Dependency Inversion perspective, this is a positive structural change even though the raw number is higher.


**Were any metrics negatively impacted?**
FANOUT and LCOM increased numerically. However: FANOUT increase reflects coupling to abstractions rather than implementations (beneficial for OCP/DIP). LCOM increase is marginal.

**Overall code quality assessment:**
The pattern implementations improved the primary maintainability metrics (LOC, WMC) for the most complex classes and introduced clear structural extensibility. The numerical increases in smells and violations are surface-level artifacts of having more analyzable source files, not indicators of worsened design. The codebase now adheres to SOLID principles — Factory centralizes creation, Adapter normalizes payment APIs, Chain separates validation concerns, Strategy makes pricing pluggable, Observer decouples event notification, and Builder simplifies complex construction.

---

## 6. Testing Evidence
Pattern-specific tests:
- `src/test/java/flight/reservation/PatternImplementationTest.java`

Existing scenario + schedule tests also pass.

Latest run summary:
- `mvn test` → **BUILD SUCCESS**
- **Tests run: 40, Failures: 0, Errors: 0, Skipped: 0**

---

## 7. Final Conclusion
The repository now includes implementation-level application of all required design patterns (Observer, Factory, Adapter, Builder, Strategy, Chain of Responsibility). The refactor preserves runtime behavior (all tests passing) and improves extensibility/maintainability by reducing hard-coded logic, centralizing creation, and separating concerns.

Post-refactor static-analysis outputs are now generated and summarized in `analysis-output/After.md`, and the before/after comparison values are populated in this report.
