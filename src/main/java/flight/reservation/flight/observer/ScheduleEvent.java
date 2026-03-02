package flight.reservation.flight.observer;

import flight.reservation.flight.ScheduledFlight;

public class ScheduleEvent {

    private final ScheduleEventType type;
    private final ScheduledFlight scheduledFlight;

    public ScheduleEvent(ScheduleEventType type, ScheduledFlight scheduledFlight) {
        this.type = type;
        this.scheduledFlight = scheduledFlight;
    }

    public ScheduleEventType getType() {
        return type;
    }

    public ScheduledFlight getScheduledFlight() {
        return scheduledFlight;
    }
}
