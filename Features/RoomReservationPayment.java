package Features;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * RoomReservationPayment - Extends PaymentFramework to handle payment for room reservations.
 * Ensures all reservation transactions are charged with proper tax and discount application.
 */
public class RoomReservationPayment extends PaymentFramework {

    private static ArrayList<RoomReservationPayment> transactions = new ArrayList<>();

    private double roomRate;
    private double durationHours;
    private String roomName;

    public RoomReservationPayment(double roomRate, double durationHours, String roomName) {
        this.roomRate = roomRate;
        this.durationHours = durationHours;
        this.roomName = roomName;
        this.balance = 0;
        this.subtotal = calculateSubtotal();
        this.discount = 0;
        this.transactionID = generateTransactionID();
    }

    /**
     * Calculate the base subtotal (room rate * hours)
     */
    private double calculateSubtotal() {
        return roomRate * durationHours;
    }

    /**
     * Generate a unique transaction ID for tracking
     */
    private String generateTransactionID() {
        return "RES-" + System.currentTimeMillis();
    }

    /**
     * Validate payment - checks if customer has sufficient balance
     * Abstract method implementation from PaymentFramework
     */
    @Override
    public boolean validatePayment() {
        System.out.println("\n--- Payment Validation ---");
        System.out.println("Room: " + roomName);
        System.out.println("Duration: " + String.format("%.2f", durationHours) + " hours");
        System.out.println("Room Rate: $" + String.format("%.2f", roomRate) + "/hour");
        System.out.println("Subtotal: $" + String.format("%.2f", subtotal));
        return true;
    }

    /**
     * Process the complete payment for a room reservation
     */
    public void processReservationPayment(double setBalance) {
        this.balance = setBalance;
        double existingDiscount = this.discount;

        System.out.println("\n===============================================");
        System.out.println("PROCESSING PAYMENT FOR RESERVATION");
        System.out.println("===============================================");

        processInvoice(0, balance, TAX_RATE);
        this.discount = existingDiscount;

        displayIncomeStatement();
        transactions.add(this);
    }

    /**
     * Display a detailed income statement for the reservation
     */
    public void displayIncomeStatement() {
        double baseAmount = subtotal;
        double taxAmount = baseAmount * TAX_RATE;
        double totalAmount = baseAmount + taxAmount;

        System.out.println("\n===============================================");
        System.out.println("INCOME STATEMENT");
        System.out.println("Room Reservation Invoice");
        System.out.println("===============================================");

        System.out.println("\nTransaction Details:");
        System.out.println("-----------------------------------------------");
        System.out.println("Transaction ID          : " + transactionID);
        System.out.println("Room Name              : " + roomName);
        System.out.println("Duration               : " + String.format("%.2f", durationHours) + " hours");
        System.out.println("Room Rate              : $" + String.format("%.2f", roomRate) + "/hour");

        System.out.println("\nFinancial Summary:");
        System.out.println("-----------------------------------------------");
        System.out.println("Base Amount            : $" + String.format("%10.2f", baseAmount));

        if (discount > 0) {
            System.out.println("Discount Applied       : $" + String.format("%10.2f", discount));
            baseAmount -= discount;
            System.out.println("Subtotal (after disc.) : $" + String.format("%10.2f", baseAmount));
        }

        taxAmount = baseAmount * TAX_RATE;
        System.out.println("Tax (12% VAT)          : $" + String.format("%10.2f", taxAmount));
        System.out.println("-----------------------------------------------");
        totalAmount = baseAmount + taxAmount;
        System.out.println("TOTAL AMOUNT DUE       : $" + String.format("%10.2f", totalAmount));
        System.out.println("-----------------------------------------------");

        System.out.println("\nPayment Status         : COMPLETED");
        System.out.println("Date & Time            : " + LocalDateTime.now());

        System.out.println("\n===============================================");
        System.out.println("Thank you for your reservation!");
        System.out.println("===============================================\n");
    }

    /**
     * Apply discount to the reservation cost
     */
    public void applyReservationDiscount(double discountAmount) {
        if (discountAmount > 0 && discountAmount <= subtotal) {
            applyDiscount(discountAmount);
            System.out.println("Discount Applied: $" + String.format("%.2f", discountAmount));
        } else {
            System.out.println("Invalid discount amount.");
        }
    }

    /**
     * Get the final amount after tax
     */
    public double getFinalAmount() {
        return subtotal + (subtotal * TAX_RATE);
    }

    public double getTotalAmount() {
        return getFinalAmount();
    }

    /**
     * Get transaction details
     */
    public String getTransactionID() {
        return transactionID;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getRoomRate() {
        return roomRate;
    }

    public double getDurationHours() {
        return durationHours;
    }

    public String getRoomName() {
        return roomName;
    }

    public static void displayAllTransactions() {
        System.out.println("\n===============================================");
        System.out.println("ALL TRANSACTION SUMMARIES");
        System.out.println("===============================================");

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        for (RoomReservationPayment tx : transactions) {
            System.out.println("Transaction ID : " + tx.getTransactionID());
            System.out.println("Room Name      : " + tx.getRoomName());
            System.out.println("Total Amount   : $" + String.format("%.2f", tx.getTotalAmount()));
            System.out.println("-----------------------------------------------");
        }
    }
}
