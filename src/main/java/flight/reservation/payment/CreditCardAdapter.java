package flight.reservation.payment;

public class CreditCardAdapter implements PaymentProcessor {

    private final CreditCard creditCard;

    public CreditCardAdapter(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    @Override
    public boolean pay(double amount) {
        if (creditCard == null || !creditCard.isValid()) {
            throw new IllegalStateException("Payment information is not set or not valid.");
        }
        double remainingAmount = creditCard.getAmount() - amount;
        if (remainingAmount < 0) {
            System.out.printf("Card limit reached - Balance: %f%n", remainingAmount);
            throw new IllegalStateException("Card limit reached");
        }
        creditCard.setAmount(remainingAmount);
        return true;
    }
}
