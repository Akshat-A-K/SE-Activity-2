# Adapter Pattern: Before Diagram

This document explains the **before** state of the payment design, i.e., the structure **prior to introducing the Adapter pattern**.

## Diagram file

- [adapter_before.puml](adapter_before.puml)

## Scope of the before diagram

The diagram highlights only the classes directly relevant to payment flow in the current implementation:

- `Order`
- `FlightOrder`
- `CreditCard`
- `Paypal`

## What the before structure shows

- `FlightOrder` inherits from `Order`.
- `FlightOrder` directly depends on concrete payment types:
  - `CreditCard` for card validation and card payment.
  - `Paypal` by reading `Paypal.DATA_BASE` directly.
- Payment logic is embedded in `FlightOrder` methods (`processOrderWithCreditCard*`, `processOrderWithPayPal`, `payWithCreditCard`, `payWithPayPal`).

## Why this matters (pre-refactor baseline)

This baseline makes the current coupling explicit:

- `FlightOrder` is tightly coupled to specific providers.
- Adding a new provider would require editing `FlightOrder`.
- `FlightOrder` depends on `Paypal` internal static storage details (`DATA_BASE`).

These points are exactly what the Adapter refactor will address in the **after** diagram using a common payment abstraction.

## Planned comparison with after diagram

When the refactor is complete, the comparison should clearly show:

- **Before:** `FlightOrder -> CreditCard` and `FlightOrder -> Paypal` (direct dependencies).
- **After:** `FlightOrder -> PaymentProcessor` (interface dependency), with adapters connecting to concrete providers.

Until then, this README documents the pre-refactor structure represented by the current before diagram.
