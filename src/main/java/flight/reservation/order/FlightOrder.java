package flight.reservation.order;

import flight.reservation.flight.ScheduledFlight;
import flight.reservation.payment.CreditCard;
import flight.reservation.payment.CreditCardAdapter;
import flight.reservation.payment.PaymentProcessor;
import flight.reservation.payment.PaypalAdapter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FlightOrder extends Order {
    private final List<ScheduledFlight> flights;
    static List<String> noFlyList = Arrays.asList("Peter", "Johannes");

    public FlightOrder(List<ScheduledFlight> flights) {
        this.flights = flights;
    }

    public static List<String> getNoFlyList() {
        return noFlyList;
    }

    public List<ScheduledFlight> getScheduledFlights() {
        return flights;
    }

    public boolean processOrderWithCreditCardDetail(String number, Date expirationDate, String cvv) throws IllegalStateException {
        CreditCard creditCard = new CreditCard(number, expirationDate, cvv);
        return processOrder(new CreditCardAdapter(creditCard));
    }

    public boolean processOrderWithCreditCard(CreditCard creditCard) throws IllegalStateException {
        return processOrder(new CreditCardAdapter(creditCard));
    }

    public boolean processOrderWithPayPal(String email, String password) throws IllegalStateException {
        return processOrder(new PaypalAdapter(email, password));
    }

    public boolean processOrder(PaymentProcessor paymentProcessor) throws IllegalStateException {
        if (isClosed()) {
            // Payment is already proceeded
            return true;
        }
        if (paymentProcessor == null) {
            throw new IllegalStateException("Payment information is not set or not valid.");
        }
        boolean isPaid = paymentProcessor.pay(this.getPrice());
        if (isPaid) {
            this.setClosed();
        }
        return isPaid;
    }

    public boolean payWithCreditCard(CreditCard card, double amount) throws IllegalStateException {
        System.out.println("Paying " + getPrice() + " using Credit Card.");
        return new CreditCardAdapter(card).pay(amount);
    }

    public boolean payWithPayPal(String email, String password, double amount) throws IllegalStateException {
        System.out.println("Paying " + getPrice() + " using PayPal.");
        return new PaypalAdapter(email, password).pay(amount);
    }
}
