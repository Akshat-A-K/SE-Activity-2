package flight.reservation.flight;

import flight.reservation.flight.builder.ScheduledFlightBuilder;
import flight.reservation.flight.observer.ScheduleEvent;
import flight.reservation.flight.observer.ScheduleEventType;
import flight.reservation.flight.observer.ScheduleObserver;
import flight.reservation.flight.observer.ScheduleSubject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Schedule implements ScheduleSubject {

    private List<ScheduledFlight> scheduledFlights;
    private final List<ScheduleObserver> observers;


    public Schedule() {
        scheduledFlights = new ArrayList<>();
        observers = new ArrayList<>();
    }

    public List<ScheduledFlight> getScheduledFlights() {
        return scheduledFlights;
    }

    public void scheduleFlight(Flight flight, Date date) {
        ScheduledFlight scheduledFlight = new ScheduledFlightBuilder()
                .withNumber(flight.getNumber())
                .withDeparture(flight.getDeparture())
                .withArrival(flight.getArrival())
                .withAircraft(flight.getAircraft())
                .withDepartureTime(date)
                .build();
        scheduledFlights.add(scheduledFlight);
        notifyObservers(new ScheduleEvent(ScheduleEventType.FLIGHT_SCHEDULED, scheduledFlight));
    }

    public void removeFlight(Flight flight) {
        List<ScheduledFlight> tbr = new ArrayList<>();
        for (ScheduledFlight scheduledFlight : scheduledFlights) {
            if (scheduledFlight == flight ||
                    (flight.getArrival() == scheduledFlight.getArrival() &&
                            flight.getDeparture() == scheduledFlight.getDeparture() &&
                            flight.getNumber() == scheduledFlight.getNumber())) {
                tbr.add(scheduledFlight);
            }
        }
        scheduledFlights.removeAll(tbr);
        tbr.forEach(scheduledFlight -> notifyObservers(new ScheduleEvent(ScheduleEventType.FLIGHT_REMOVED, scheduledFlight)));
    }

    public void removeScheduledFlight(ScheduledFlight flight) {
        scheduledFlights.remove(flight);
        notifyObservers(new ScheduleEvent(ScheduleEventType.FLIGHT_REMOVED, flight));
    }

    public ScheduledFlight searchScheduledFlight(int flightNumber) {
        return scheduledFlights.stream()
                .filter(f -> f.getNumber() == flightNumber)
                .findFirst()
                .orElse(null);
    }

    public void clear() {
        scheduledFlights.clear();
    }

    @Override
    public void addObserver(ScheduleObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ScheduleObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(ScheduleEvent event) {
        observers.forEach(observer -> observer.onScheduleEvent(event));
    }
}
