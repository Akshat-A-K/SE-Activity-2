package flight.reservation.order.builder;

import flight.reservation.Customer;
import flight.reservation.Passenger;
import flight.reservation.flight.ScheduledFlight;
import flight.reservation.order.FlightOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FlightOrderBuilder {

    private Customer customer;
    private List<ScheduledFlight> flights;
    private List<String> passengerNames;
    private double price;

    public FlightOrderBuilder withCustomer(Customer customer) {
        this.customer = customer;
        return this;
    }

    public FlightOrderBuilder withFlights(List<ScheduledFlight> flights) {
        this.flights = flights;
        return this;
    }

    public FlightOrderBuilder withPassengerNames(List<String> passengerNames) {
        this.passengerNames = passengerNames;
        return this;
    }

    public FlightOrderBuilder withPrice(double price) {
        this.price = price;
        return this;
    }

    public FlightOrder build() {
        List<ScheduledFlight> safeFlights = flights == null ? new ArrayList<>() : flights;
        List<String> safePassengerNames = passengerNames == null ? new ArrayList<>() : passengerNames;

        FlightOrder order = new FlightOrder(safeFlights);
        order.setCustomer(customer);
        order.setPrice(price);

        List<Passenger> passengers = safePassengerNames.stream().map(Passenger::new).collect(Collectors.toList());
        order.setPassengers(passengers);
        order.getScheduledFlights().forEach(scheduledFlight -> scheduledFlight.addPassengers(passengers));

        return order;
    }
}
