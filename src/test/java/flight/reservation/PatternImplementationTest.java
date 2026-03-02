package flight.reservation;

import flight.reservation.flight.Flight;
import flight.reservation.flight.Schedule;
import flight.reservation.flight.ScheduledFlight;
import flight.reservation.flight.builder.ScheduledFlightBuilder;
import flight.reservation.flight.observer.ScheduleEvent;
import flight.reservation.flight.observer.ScheduleEventType;
import flight.reservation.flight.observer.ScheduleObserver;
import flight.reservation.flight.pricing.DemandPricing;
import flight.reservation.flight.pricing.FixedPricing;
import flight.reservation.order.FlightOrder;
import flight.reservation.order.builder.FlightOrderBuilder;
import flight.reservation.order.validation.CapacityValidator;
import flight.reservation.order.validation.NoFlyValidator;
import flight.reservation.order.validation.OrderValidationContext;
import flight.reservation.order.validation.OrderValidationHandler;
import flight.reservation.payment.CreditCard;
import flight.reservation.payment.CreditCardAdapter;
import flight.reservation.payment.PaypalAdapter;
import flight.reservation.plane.Aircraft;
import flight.reservation.plane.AircraftFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pattern Implementation Tests")
public class PatternImplementationTest {

    private Airport berlin() {
        return new Airport("Berlin Airport", "BER", "Berlin, Berlin");
    }

    private Airport frankfurt() {
        return new Airport("Frankfurt Airport", "FRA", "Frankfurt, Hesse");
    }

    private ScheduledFlight createScheduledFlight(Aircraft aircraft) {
        Flight flight = new Flight(101, berlin(), frankfurt(), aircraft);
        Schedule schedule = new Schedule();
        Date departure = TestUtil.addDays(Date.from(Instant.now()), 2);
        schedule.scheduleFlight(flight, departure);
        return schedule.searchScheduledFlight(flight.getNumber());
    }

    @Test
    @DisplayName("Factory: creates concrete aircraft implementations")
    void factoryCreatesAircraft() throws NoSuchFieldException {
        Aircraft plane = AircraftFactory.createAircraft("plane", "A380");
        Aircraft helicopter = AircraftFactory.createAircraft("helicopter", "H1");
        Aircraft drone = AircraftFactory.createAircraft("drone", "HypaHype");

        assertEquals("A380", plane.getModel());
        assertEquals(500, plane.getPassengerCapacity());
        assertEquals(42, plane.getCrewCapacity());

        assertEquals("H1", helicopter.getModel());
        assertEquals(4, helicopter.getPassengerCapacity());
        assertEquals(2, helicopter.getCrewCapacity());

        assertEquals("HypaHype", drone.getModel());
        assertEquals(4, drone.getPassengerCapacity());
        assertEquals(0, drone.getCrewCapacity());

        ScheduledFlight scheduledFlight = createScheduledFlight(plane);
        assertEquals(500, scheduledFlight.getCapacity());
    }

    @Test
    @DisplayName("Adapter: CreditCardAdapter and PaypalAdapter are used through FlightOrder")
    void paymentAdaptersWorkThroughOrder() {
        Customer customer = new Customer("Alice", "amanda@ya.com");
        ScheduledFlight scheduledFlight = createScheduledFlight(AircraftFactory.createAircraft("plane", "A350"));

        FlightOrder orderWithCard = customer.createOrder(Arrays.asList("P1"), Arrays.asList(scheduledFlight), 100);
        CreditCard card = new CreditCard("4111111111111111", TestUtil.addDays(Date.from(Instant.now()), 30), "123");
        assertTrue(orderWithCard.processOrderWithCreditCard(card));
        assertTrue(orderWithCard.isClosed());

        FlightOrder orderWithPaypal = customer.createOrder(Arrays.asList("P2"), Arrays.asList(scheduledFlight), 50);
        assertTrue(orderWithPaypal.processOrderWithPayPal("amanda@ya.com", "amanda1985"));
        assertTrue(orderWithPaypal.isClosed());
    }

    @Test
    @DisplayName("Chain: no-fly and capacity validators compose correctly")
    void validationChainWorks() {
        Customer validCustomer = new Customer("Alice", "a@b.com");
        Customer blockedCustomer = new Customer("Peter", "p@b.com");
        ScheduledFlight scheduledFlight = createScheduledFlight(AircraftFactory.createAircraft("helicopter", "H1"));

        OrderValidationHandler chain = new NoFlyValidator();
        chain.setNext(new CapacityValidator());

        OrderValidationContext validContext = new OrderValidationContext(validCustomer, Arrays.asList("X", "Y"), Arrays.asList(scheduledFlight));
        assertTrue(chain.validate(validContext));

        OrderValidationContext blockedContext = new OrderValidationContext(blockedCustomer, Arrays.asList("X"), Arrays.asList(scheduledFlight));
        assertFalse(chain.validate(blockedContext));

        OrderValidationContext overCapacityContext = new OrderValidationContext(validCustomer,
                Arrays.asList("A", "B", "C", "D", "E"), Arrays.asList(scheduledFlight));
        assertFalse(chain.validate(overCapacityContext));
    }

    @Test
    @DisplayName("Strategy: fixed and demand pricing produce expected prices")
    void pricingStrategiesWork() {
        ScheduledFlight scheduledFlight = createScheduledFlight(AircraftFactory.createAircraft("helicopter", "H1"));
        scheduledFlight.setCurrentPrice(200);

        scheduledFlight.setPricingStrategy(new FixedPricing());
        assertEquals(200, scheduledFlight.applyPricingStrategy());

        scheduledFlight.addPassengers(Arrays.asList(new Passenger("A"), new Passenger("B")));
        scheduledFlight.setPricingStrategy(new DemandPricing());
        double demandPrice = scheduledFlight.applyPricingStrategy();

        assertEquals(250.0, demandPrice);
        assertTrue(demandPrice > 200);
    }

    @Test
    @DisplayName("Observer: schedule and scheduled-flight observers receive events")
    void observerNotificationsWork() {
        Schedule schedule = new Schedule();
        TestObserver scheduleObserver = new TestObserver();
        schedule.addObserver(scheduleObserver);

        Flight flight = new Flight(222, berlin(), frankfurt(), AircraftFactory.createAircraft("plane", "A350"));
        Date departure = TestUtil.addDays(Date.from(Instant.now()), 5);

        schedule.scheduleFlight(flight, departure);
        assertEquals(1, scheduleObserver.events.size());
        assertEquals(ScheduleEventType.FLIGHT_SCHEDULED, scheduleObserver.events.get(0).getType());

        ScheduledFlight sf = schedule.searchScheduledFlight(222);
        schedule.removeScheduledFlight(sf);
        assertEquals(2, scheduleObserver.events.size());
        assertEquals(ScheduleEventType.FLIGHT_REMOVED, scheduleObserver.events.get(1).getType());

        ScheduledFlight pricedFlight = createScheduledFlight(AircraftFactory.createAircraft("drone", "HypaHype"));
        TestObserver priceObserver = new TestObserver();
        pricedFlight.addObserver(priceObserver);

        pricedFlight.setCurrentPrice(180);
        assertEquals(1, priceObserver.events.size());
        assertEquals(ScheduleEventType.PRICE_UPDATED, priceObserver.events.get(0).getType());
    }

    @Test
    @DisplayName("Builder: FlightOrderBuilder and ScheduledFlightBuilder build valid objects")
    void buildersConstructObjects() throws NoSuchFieldException {
        Customer customer = new Customer("BuilderUser", "build@x.com");
        Aircraft plane = AircraftFactory.createAircraft("plane", "Embraer 190");

        ScheduledFlight scheduledFlight = new ScheduledFlightBuilder()
                .withNumber(333)
                .withDeparture(berlin())
                .withArrival(frankfurt())
                .withAircraft(plane)
                .withDepartureTime(TestUtil.addDays(Date.from(Instant.now()), 3))
                .withCurrentPrice(300)
                .build();

        assertEquals(300, scheduledFlight.getCurrentPrice());
        assertEquals(25, scheduledFlight.getCapacity());

        FlightOrder order = new FlightOrderBuilder()
                .withCustomer(customer)
                .withFlights(Arrays.asList(scheduledFlight))
                .withPassengerNames(Arrays.asList("One", "Two"))
                .withPrice(600)
                .build();

        assertEquals(customer, order.getCustomer());
        assertEquals(2, order.getPassengers().size());
        assertEquals(2, scheduledFlight.getPassengers().size());
        assertEquals(600, order.getPrice());
    }

    private static class TestObserver implements ScheduleObserver {
        private final List<ScheduleEvent> events = new ArrayList<>();

        @Override
        public void onScheduleEvent(ScheduleEvent event) {
            events.add(event);
        }
    }
}
