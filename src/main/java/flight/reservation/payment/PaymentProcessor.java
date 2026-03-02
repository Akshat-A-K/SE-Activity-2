package flight.reservation.payment;

public interface PaymentProcessor {

    boolean pay(double amount);
}
