package Features;

public abstract class PaymentFramework {

    protected static final double TAX_RATE = 0.12;

    protected double balance;
    protected double subtotal;
    protected double discount;
    protected String transactionID;

    public abstract boolean validatePayment();

    public void applyTax() {
        double taxAmount = subtotal * TAX_RATE;
        System.out.println("VAT Amount (12%): $" + String.format("%.2f", taxAmount));
    }

    public void applyDiscount(double discountAmount) {
        this.discount = discountAmount;
        if (discountAmount > 0 && discountAmount <= subtotal) {
            this.subtotal -= discountAmount;
        }
    }

    public void computeTotal() {
        double total = subtotal + (subtotal * TAX_RATE);
        System.out.println("Final Computed Total: $" + String.format("%.2f", total));
    }

    public void finalizeTransaction() {
        System.out.println("Transaction " + transactionID + " finalized.");
    }

    public void processInvoice(double discount, double balance, double taxRate) {
        this.discount = discount;
        this.balance = balance;

        if (validatePayment()) {
            if (discount > 0) {
                applyDiscount(discount);
            }
            applyTax();
            computeTotal();
            finalizeTransaction();
        } else {
            System.out.println("Payment Failed.");
        }
    }
}
