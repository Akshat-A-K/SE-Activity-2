package flight.reservation.flight.pricing;

import flight.reservation.flight.ScheduledFlight;

public interface PricingStrategy {

    double compute(ScheduledFlight scheduledFlight);
}
