**Design Patterns Detected**

This document summarizes candidate design patterns for the Reservation-System-Starter codebase, where they apply, why they help, and concrete class/interface suggestions.

**Observer Pattern**:
- **Where:** `Schedule` / `ScheduledFlight` as subjects; `Customer` / `Order` or dedicated listeners as observers.
- **Why:** Notify interested parties when flights are added/removed/delayed or when `ScheduledFlight` price changes (`setCurrentPrice`). Decouples schedule events from notification/booking logic.
- **Suggested types:** `ScheduleObserver`, `ScheduleEvent`, `ScheduleSubject`.

**Factory Pattern**:
- **Where:** Aircraft creation currently spread and stored as `Object aircraft` in `Flight`/`ScheduledFlight`.
- **Why:** Replace `instanceof` checks with an `Aircraft` abstraction and centralize creation.
- **Suggested types:** `interface Aircraft`, `class AircraftFactory { create(type, model) }`, concrete `PassengerPlane`, `Helicopter`, `PassengerDrone` implementing `Aircraft`.

**Adapter Pattern**:
- **Where:** Payment integration (`CreditCard`, `Paypal`) used directly inside `FlightOrder`.
- **Why:** Provide a uniform `PaymentProcessor` interface to make adding providers easier and to remove direct coupling to `CreditCard` and `Paypal.DATA_BASE`.
- **Suggested types:** `interface PaymentProcessor { boolean pay(double) }`, `CreditCardAdapter`, `PaypalAdapter`.

**Builder Pattern**:
- **Where:** Construction of complex objects like `FlightOrder` or `ScheduledFlight`.
- **Why:** Improve readability for multi-step object creation and support optional fields (e.g., price, departureTime).
- **Suggested types:** `FlightOrderBuilder`, `ScheduledFlightBuilder`.

**Strategy Pattern**:
- **Where:** Pricing and payment selection logic.
- **Why:** Make pricing policies and payment algorithms pluggable and testable (e.g., `FixedPricing`, `DemandPricing`).
- **Suggested types:** `interface PricingStrategy { double compute(ScheduledFlight) }`, `interface PaymentStrategy { boolean pay(Order) }`.

**Chain of Responsibility / Command Pattern**:
- **Where:** `FlightOrder` mixes validation, payment, and closing logic.
- **Why:** Break the processing flow into independent handlers: validators (no-fly list, capacity), payment handler, confirmation handler. Commands can represent each action for undo/retry.
- **Suggested types:** `OrderHandler` chain with `NoFlyValidator`, `CapacityValidator`, `PaymentHandler`, `ConfirmationHandler`.

**Prioritized refactor plan**:
1. Add `Aircraft` interface + `AircraftFactory` and replace `Object aircraft` usage.
2. Introduce `PaymentProcessor` and adapters; refactor `FlightOrder` to use them.
3. Extract order validation into a chain of responsibility (`OrderValidator` chain).
4. Add `PricingStrategy` for `ScheduledFlight` price computation.
5. Add `Observer` support on `Schedule` for notifications and UI updates.
6. Add `Builder` types for complex object creation (orders, scheduled flights).

**Next steps**:
- Implement step 1 (`Aircraft` + `AircraftFactory`) to remove `instanceof` checks.
- Optionally implement step 2 (payment adapters) in parallel.

If you want, I can implement step 1 now (create `Aircraft` interface, `AircraftFactory`, and refactor `Flight`/`ScheduledFlight`).
