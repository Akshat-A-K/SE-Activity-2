package flight.reservation.flight;

import flight.reservation.Airport;
import flight.reservation.Passenger;
import flight.reservation.flight.observer.ScheduleEvent;
import flight.reservation.flight.observer.ScheduleEventType;
import flight.reservation.flight.observer.ScheduleObserver;
import flight.reservation.flight.observer.ScheduleSubject;
import flight.reservation.flight.pricing.FixedPricing;
import flight.reservation.flight.pricing.PricingStrategy;
import flight.reservation.plane.Aircraft;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScheduledFlight extends Flight implements ScheduleSubject {

    private final List<Passenger> passengers;
    private final Date departureTime;
    private double currentPrice = 100;
    private double basePrice = 100;
    private PricingStrategy pricingStrategy;
    private final List<ScheduleObserver> observers;

    public ScheduledFlight(int number, Airport departure, Airport arrival, Aircraft aircraft, Date departureTime) {
        super(number, departure, arrival, aircraft);
        this.departureTime = departureTime;
        this.passengers = new ArrayList<>();
        this.pricingStrategy = new FixedPricing();
        this.observers = new ArrayList<>();
    }

    public ScheduledFlight(int number, Airport departure, Airport arrival, Aircraft aircraft, Date departureTime, double currentPrice) {
        super(number, departure, arrival, aircraft);
        this.departureTime = departureTime;
        this.passengers = new ArrayList<>();
        this.currentPrice = currentPrice;
        this.basePrice = currentPrice;
        this.pricingStrategy = new FixedPricing();
        this.observers = new ArrayList<>();
    }

    public int getCrewMemberCapacity() throws NoSuchFieldException {
        return this.aircraft.getCrewCapacity();
    }

    public void addPassengers(List<Passenger> passengers) {
        this.passengers.addAll(passengers);
    }

    public void removePassengers(List<Passenger> passengers) {
        this.passengers.removeAll(passengers);
    }

    public int getCapacity() throws NoSuchFieldException {
        return this.aircraft.getPassengerCapacity();
    }

    public int getAvailableCapacity() throws NoSuchFieldException {
        return this.getCapacity() - this.passengers.size();
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
        this.basePrice = currentPrice;
        notifyObservers(new ScheduleEvent(ScheduleEventType.PRICE_UPDATED, this));
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = pricingStrategy;
    }

    public double applyPricingStrategy() {
        this.currentPrice = pricingStrategy.compute(this);
        notifyObservers(new ScheduleEvent(ScheduleEventType.PRICE_UPDATED, this));
        return this.currentPrice;
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
