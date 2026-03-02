package flight.reservation;

import flight.reservation.flight.ScheduledFlight;
import flight.reservation.order.FlightOrder;
import flight.reservation.order.Order;
import flight.reservation.order.builder.FlightOrderBuilder;
import flight.reservation.order.validation.CapacityValidator;
import flight.reservation.order.validation.NoFlyValidator;
import flight.reservation.order.validation.OrderValidationContext;
import flight.reservation.order.validation.OrderValidationHandler;

import java.util.ArrayList;
import java.util.List;

public class Customer {

    private String email;
    private String name;
    private List<Order> orders;

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
        this.orders = new ArrayList<>();
    }

    public FlightOrder createOrder(List<String> passengerNames, List<ScheduledFlight> flights, double price) {
        if (!isOrderValid(passengerNames, flights)) {
            throw new IllegalStateException("Order is not valid");
        }

        FlightOrder order = new FlightOrderBuilder()
                .withCustomer(this)
                .withFlights(flights)
                .withPassengerNames(passengerNames)
                .withPrice(price)
                .build();

        orders.add(order);
        return order;
    }

    private boolean isOrderValid(List<String> passengerNames, List<ScheduledFlight> flights) {
        OrderValidationContext context = new OrderValidationContext(this, passengerNames, flights);
        OrderValidationHandler chain = new NoFlyValidator();
        chain.setNext(new CapacityValidator());
        return chain.validate(context);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

}
