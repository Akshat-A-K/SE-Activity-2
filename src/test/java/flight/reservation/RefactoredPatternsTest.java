package flight.reservation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Import relevant classes for each pattern
import flight.reservation.plane.AircraftFactory;
import flight.reservation.plane.Aircraft;
import flight.reservation.payment.CreditCardAdapter;
import flight.reservation.payment.PaypalAdapter;
import flight.reservation.payment.PaymentProcessor;
import flight.reservation.payment.CreditCard;
import flight.reservation.order.validation.*;
import flight.reservation.flight.pricing.*;
import flight.reservation.flight.observer.*;
import flight.reservation.order.builder.FlightOrderBuilder;
import flight.reservation.flight.builder.ScheduledFlightBuilder;
import flight.reservation.flight.ScheduledFlight;
import flight.reservation.order.FlightOrder;
import flight.reservation.Customer;
import java.util.*;

public class RefactoredPatternsTest {
    // Factory Pattern
    @Test
    void testFactoryCreatesPlane() {
        Aircraft plane = AircraftFactory.createAircraft("plane", "A350");
        assertNotNull(plane);
        assertEquals("A350", plane.getModel());
    }

    @Test
    void testFactoryCreatesHelicopter() {
        Aircraft heli = AircraftFactory.createAircraft("helicopter", "H1");
        assertNotNull(heli);
        assertEquals("H1", heli.getModel());
    }

    @Test
    void testFactoryThrowsOnUnknownType() {
        assertThrows(IllegalArgumentException.class, () -> AircraftFactory.createAircraft("unknown", "X"));
    }

    // Adapter Pattern
    @Test
    void testCreditCardAdapterValidPayment() {
        CreditCard validCard = new CreditCard("1234567890123456", new Date(System.currentTimeMillis() + 1000000000), "123");
        PaymentProcessor cc = new CreditCardAdapter(validCard);
        assertTrue(cc.pay(100.0));
    }

    @Test
    void testPaypalAdapterValidPayment() {
        PaymentProcessor paypal = new PaypalAdapter("amanda@ya.com", "amanda1985");
        assertTrue(paypal.pay(50.0));
    }

    @Test
    void testCreditCardAdapterInvalidCard() {
        CreditCard invalidCard = new CreditCard("", new Date(System.currentTimeMillis() - 1000000000), "000");
        PaymentProcessor cc = new CreditCardAdapter(invalidCard);
        assertThrows(IllegalStateException.class, () -> cc.pay(100.0));
    }

    // Chain of Responsibility
    @Test
    void testOrderValidationChainValid() {
        Customer customer = new Customer("TestUser", "test@example.com");
        Airport dep = new Airport("Dep", "D", "Loc");
        Airport arr = new Airport("Arr", "A", "Loc");
        Aircraft aircraft = AircraftFactory.createAircraft("plane", "A350");
        ScheduledFlight flight = new ScheduledFlight(1, dep, arr, aircraft, new Date(System.currentTimeMillis() + 1000000));
        List<String> passengerNames = Arrays.asList("Alice");
        List<ScheduledFlight> flights = Arrays.asList(flight);
        OrderValidationContext ctx = new OrderValidationContext(customer, passengerNames, flights);
        OrderValidationHandler chain = new NoFlyValidator();
        chain.setNext(new CapacityValidator());
        assertTrue(chain.validate(ctx));
    }

    @Test
    void testOrderValidationChainInvalidCapacity() {
        Customer customer = new Customer("TestUser", "test@example.com");
        Airport dep = new Airport("Dep", "D", "Loc");
        Airport arr = new Airport("Arr", "A", "Loc");
        Aircraft aircraft = AircraftFactory.createAircraft("plane", "Embraer 190");
        ScheduledFlight flight = new ScheduledFlight(1, dep, arr, aircraft, new Date(System.currentTimeMillis() + 1000000));
        List<String> passengerNames = Arrays.asList(
            "Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Heidi", "Ivan", "Judy",
            "Mallory", "Niaj", "Olivia", "Peggy", "Sybil", "Trent", "Victor", "Walter", "Xavier", "Yvonne",
            "Zara", "Quinn", "Ruth", "Steve", "Tom", "Uma" // 26 passengers, capacity is 25
        );
        List<ScheduledFlight> flights = Arrays.asList(flight);
        OrderValidationContext ctx = new OrderValidationContext(customer, passengerNames, flights);
        OrderValidationHandler chain = new NoFlyValidator();
        chain.setNext(new CapacityValidator());
        assertFalse(chain.validate(ctx));
    }

    // Strategy Pattern
    @Test
    void testFixedPricingStrategy() {
        Airport dep = new Airport("Dep", "D", "Loc");
        Airport arr = new Airport("Arr", "A", "Loc");
        Aircraft aircraft = AircraftFactory.createAircraft("plane", "A350");
        ScheduledFlight flight = new ScheduledFlight(1, dep, arr, aircraft, new Date(System.currentTimeMillis() + 1000000), 200.0);
        PricingStrategy strategy = new FixedPricing();
        flight.setPricingStrategy(strategy);
        assertEquals(200.0, flight.applyPricingStrategy(), 0.01);
    }

    @Test
    void testDemandPricingStrategy() {
        Airport dep = new Airport("Dep", "D", "Loc");
        Airport arr = new Airport("Arr", "A", "Loc");
        Aircraft aircraft = AircraftFactory.createAircraft("plane", "A350");
        ScheduledFlight flight = new ScheduledFlight(1, dep, arr, aircraft, new Date(System.currentTimeMillis() + 1000000), 100.0);
        PricingStrategy strategy = new DemandPricing();
        flight.setPricingStrategy(strategy);
        double price = flight.applyPricingStrategy();
        assertTrue(price > 0);
    }

    // Observer Pattern
    @Test
    void testScheduleObserverReceivesEvent() {
        Airport dep = new Airport("Dep", "D", "Loc");
        Airport arr = new Airport("Arr", "A", "Loc");
        Aircraft aircraft = AircraftFactory.createAircraft("plane", "A350");
        ScheduledFlight flight = new ScheduledFlight(1, dep, arr, aircraft, new Date(System.currentTimeMillis() + 1000000));
        List<ScheduleEvent> events = new ArrayList<>();
        flight.addObserver(event -> events.add(event));
        flight.notifyObservers(new ScheduleEvent(ScheduleEventType.FLIGHT_SCHEDULED, flight));
        assertFalse(events.isEmpty());
        assertEquals(ScheduleEventType.FLIGHT_SCHEDULED, events.get(0).getType());
    }

    // Builder Pattern
    @Test
    void testFlightOrderBuilderCreatesOrder() {
        Customer customer = new Customer("TestUser", "test@example.com");
        Airport dep = new Airport("Dep", "D", "Loc");
        Airport arr = new Airport("Arr", "A", "Loc");
        Aircraft aircraft = AircraftFactory.createAircraft("plane", "A350");
        ScheduledFlight flight = new ScheduledFlight(1, dep, arr, aircraft, new Date(System.currentTimeMillis() + 1000000));
        List<ScheduledFlight> flights = Arrays.asList(flight);
        List<String> passengerNames = Arrays.asList("Alice");
        FlightOrder order = new FlightOrderBuilder()
                .withCustomer(customer)
                .withFlights(flights)
                .withPassengerNames(passengerNames)
                .withPrice(300.0)
                .build();
        assertNotNull(order);
        assertEquals(300.0, order.getPrice(), 0.01);
    }

    @Test
    void testScheduledFlightBuilderCreatesFlight() {
        Airport dep = new Airport("Dep", "D", "Loc");
        Airport arr = new Airport("Arr", "A", "Loc");
        Aircraft aircraft = AircraftFactory.createAircraft("plane", "A350");
        ScheduledFlight flight = new ScheduledFlightBuilder()
                .withNumber(1)
                .withDeparture(dep)
                .withArrival(arr)
                .withAircraft(aircraft)
                .withDepartureTime(new Date(System.currentTimeMillis() + 1000000))
                .build();
        assertNotNull(flight);
        assertEquals("A350", flight.getAircraft().getModel());
    }

    // Integration Test
    @Test
    void testIntegrationFactoryAdapterStrategyObserverBuilder() {
        Airport dep = new Airport("Dep", "D", "Loc");
        Airport arr = new Airport("Arr", "A", "Loc");
        Aircraft aircraft = AircraftFactory.createAircraft("plane", "A350");
        ScheduledFlight flight = new ScheduledFlightBuilder()
                .withNumber(1)
                .withDeparture(dep)
                .withArrival(arr)
                .withAircraft(aircraft)
                .withDepartureTime(new Date(System.currentTimeMillis() + 1000000))
                .build();
        PricingStrategy strategy = new FixedPricing();
        flight.setPricingStrategy(strategy);
        List<ScheduleEvent> events = new ArrayList<>();
        flight.addObserver(event -> events.add(event));
        flight.notifyObservers(new ScheduleEvent(ScheduleEventType.FLIGHT_SCHEDULED, flight));
        assertEquals(100.0, flight.applyPricingStrategy(), 0.01);
        assertFalse(events.isEmpty());
    }
}
