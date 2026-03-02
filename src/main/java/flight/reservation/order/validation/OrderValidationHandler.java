package flight.reservation.order.validation;

public abstract class OrderValidationHandler {

    private OrderValidationHandler next;

    public OrderValidationHandler setNext(OrderValidationHandler next) {
        this.next = next;
        return next;
    }

    public boolean validate(OrderValidationContext context) {
        if (!check(context)) {
            return false;
        }
        if (next == null) {
            return true;
        }
        return next.validate(context);
    }

    protected abstract boolean check(OrderValidationContext context);
}
