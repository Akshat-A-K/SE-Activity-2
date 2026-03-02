package flight.reservation.order.validation;

public class CapacityValidator extends OrderValidationHandler {

    @Override
    protected boolean check(OrderValidationContext context) {
        return context.getFlights().stream().allMatch(scheduledFlight -> {
            try {
                return scheduledFlight.getAvailableCapacity() >= context.getPassengerNames().size();
            } catch (NoSuchFieldException e) {
                return false;
            }
        });
    }
}
