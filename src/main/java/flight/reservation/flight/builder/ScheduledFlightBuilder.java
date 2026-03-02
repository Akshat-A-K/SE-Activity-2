package flight.reservation.flight.builder;

import flight.reservation.Airport;
import flight.reservation.flight.ScheduledFlight;
import flight.reservation.plane.Aircraft;

import java.util.Date;

public class ScheduledFlightBuilder {

    private int number;
    private Airport departure;
    private Airport arrival;
    private Aircraft aircraft;
    private Date departureTime;
    private Double currentPrice;

    public ScheduledFlightBuilder withNumber(int number) {
        this.number = number;
        return this;
    }

    public ScheduledFlightBuilder withDeparture(Airport departure) {
        this.departure = departure;
        return this;
    }

    public ScheduledFlightBuilder withArrival(Airport arrival) {
        this.arrival = arrival;
        return this;
    }

    public ScheduledFlightBuilder withAircraft(Aircraft aircraft) {
        this.aircraft = aircraft;
        return this;
    }

    public ScheduledFlightBuilder withDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
        return this;
    }

    public ScheduledFlightBuilder withCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
        return this;
    }

    public ScheduledFlight build() {
        if (currentPrice == null) {
            return new ScheduledFlight(number, departure, arrival, aircraft, departureTime);
        }
        return new ScheduledFlight(number, departure, arrival, aircraft, departureTime, currentPrice);
    }
}
