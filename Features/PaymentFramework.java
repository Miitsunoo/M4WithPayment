package Features;

public abstract class PaymentFramework {

    protected static final double TAX_RATE = 0.12;

    protected double balance;
    protected double subtotal;
    protected double discount;
    protected String transactionID;

    public abstract boolean validatePayment();

    public void applyTax() {
        // Logic for tax calculation
        double taxAmount = subtotal - (subtotal / (1 + TAX_RATE));
        System.out.println("VAT Amount (12%): " + taxAmount);
    }

    public void applyDiscount(double discountAmount) {
        this.discount = discountAmount;
        this.subtotal -= discountAmount;
    }

    public void computeTotal() {
        System.out.println("Final Computed Total: " + this.subtotal);
    }

    public void finalizeTransaction() {
        System.out.println("Transaction " + transactionID + " finalized.");
    }

    public void processInvoice(double discount, double balance, double tax) {
        this.discount = discount;
        this.balance = balance;

        if (validatePayment()) {
            applyDiscount(this.discount);
            applyDiscount(this.discount);
            applyTax();
            computeTotal();
            finalizeTransaction();
        } else {
            System.out.println("Payment Failed.");
        }
    }
}
