package flight.reservation.flight.pricing;

import flight.reservation.flight.ScheduledFlight;

public class DemandPricing implements PricingStrategy {

    @Override
    public double compute(ScheduledFlight scheduledFlight) {
        try {
            int capacity = scheduledFlight.getCapacity();
            if (capacity == 0) {
                return scheduledFlight.getBasePrice();
            }
            double loadFactor = (double) scheduledFlight.getPassengers().size() / capacity;
            return scheduledFlight.getBasePrice() * (1 + (0.5 * loadFactor));
        } catch (NoSuchFieldException e) {
            return scheduledFlight.getBasePrice();
        }
    }
}
