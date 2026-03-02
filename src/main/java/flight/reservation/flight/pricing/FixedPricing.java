package flight.reservation.flight.pricing;

import flight.reservation.flight.ScheduledFlight;

public class FixedPricing implements PricingStrategy {

    @Override
    public double compute(ScheduledFlight scheduledFlight) {
        return scheduledFlight.getBasePrice();
    }
}
