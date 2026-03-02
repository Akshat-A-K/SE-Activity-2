package flight.reservation.order.validation;

import flight.reservation.order.FlightOrder;

public class NoFlyValidator extends OrderValidationHandler {

    @Override
    protected boolean check(OrderValidationContext context) {
        if (FlightOrder.getNoFlyList().contains(context.getCustomer().getName())) {
            return false;
        }
        return context.getPassengerNames().stream().noneMatch(passenger -> FlightOrder.getNoFlyList().contains(passenger));
    }
}
