package flight.reservation.payment;

public class PaypalAdapter implements PaymentProcessor {

    private final String email;
    private final String password;

    public PaypalAdapter(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public boolean pay(double amount) {
        if (email == null || password == null || !email.equals(Paypal.DATA_BASE.get(password))) {
            throw new IllegalStateException("Payment information is not set or not valid.");
        }
        return true;
    }
}
