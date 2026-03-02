package flight.reservation.flight.observer;

public interface ScheduleSubject {

    void addObserver(ScheduleObserver observer);

    void removeObserver(ScheduleObserver observer);

    void notifyObservers(ScheduleEvent event);
}
