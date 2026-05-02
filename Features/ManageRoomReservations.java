package Features;

import java.util.ArrayList;
import java.util.Scanner;

public class ManageRoomReservations {

    // POLYMORPHISM: List of superclass
    static ArrayList<Reservation> reservations = new ArrayList<>();
    static int nextId = 1;
    static String currentUserName = null; // Stores logged-in user's name

    /**
     * Set the current logged-in user's name from the Main login system.
     * Called by Main.java after successful login.
     */
    public static void setCurrentUser(String userName) {
        currentUserName = userName;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n======================================");
            System.out.println("     BUNKER ROOM RESERVATION SYSTEM");
            System.out.println("======================================");
            System.out.println("[1] Reserve Room / Space");
            System.out.println("[2] View Schedule & Availability");
            System.out.println("[3] View All Reservations");
            System.out.println("[4] Exit");
            System.out.print("Select Option: ");

            try {
                choice = sc.nextInt(); sc.nextLine();
            } catch (Exception e) {
                sc.nextLine();
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1: reserveRoom(sc); break;
                case 2: checkAvailability(sc); break;
                case 3: viewAll(); break;
                case 4: System.out.println("Exiting system..."); break;
                default: System.out.println("Invalid choice.");
            }

        } while (choice != 4);
    }

    /**
     * Parse time from schedule string (e.g., "10AM" -> minutes from start of day)
     */
    static int parseTimeToMinutes(String timeStr) {
        try {
            timeStr = timeStr.trim().toUpperCase();
            
            if (timeStr.endsWith("PM")) {
                int hour = Integer.parseInt(timeStr.substring(0, timeStr.length() - 2));
                if (hour != 12) hour += 12;  // Convert to 24-hour format
                return hour * 60;
            } else if (timeStr.endsWith("AM")) {
                int hour = Integer.parseInt(timeStr.substring(0, timeStr.length() - 2));
                if (hour == 12) hour = 0;  // 12AM is midnight
                return hour * 60;
            }
        } catch (Exception e) {
            return -1;
        }
        return -1;
    }

    /**
     * Extract start and end times from schedule string (e.g., "10AM-12PM")
     */
    static int[] getScheduleTimeRange(String schedule) {
        try {
            String[] parts = schedule.split("-");
            if (parts.length == 2) {
                int startTime = parseTimeToMinutes(parts[0]);
                int endTime = parseTimeToMinutes(parts[1]);
                if (startTime < 0 || endTime < 0) return null;
                return new int[]{startTime, endTime};
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    /**
     * Calculate duration in hours from schedule (e.g., "10AM-12PM" -> 2 hours)
     */
    static int calculateDurationFromSchedule(String schedule) {
        int[] timeRange = getScheduleTimeRange(schedule);
        if (timeRange == null) return 0;
        return (timeRange[1] - timeRange[0]) / 60;
    }

    /**
     * Check if two time ranges overlap
     */
    static boolean timesOverlap(int[] range1, int[] range2) {
        if (range1 == null || range2 == null) return false;
        // Two ranges overlap if: start1 < end2 AND start2 < end1
        return range1[0] < range2[1] && range2[0] < range1[1];
    }

    /**
     * Check if a room is available for the specified schedule
     */
    static boolean isRoomAvailable(String room, String schedule) {
        int[] requestedTime = getScheduleTimeRange(schedule);
        if (requestedTime == null) {
            System.out.println("Invalid schedule format. Use format like '10AM-12PM'");
            return false;
        }

        for (Reservation r : reservations) {
            if (r.getRoom().equalsIgnoreCase(room)) {
                int[] existingTime = getScheduleTimeRange(r.getSchedule());
                if (existingTime != null && timesOverlap(requestedTime, existingTime)) {
                    System.out.println("Conflict detected! Time slot " + schedule + " overlaps with existing reservation at " + r.getSchedule());
                    return false;
                }
            }
        }
        return true;
    }

    // FEATURE 1: ROOM & SPACE RESERVATION WITH PAYMENT INTEGRATION
    static void reserveRoom(Scanner sc) {

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   ROOM / SPACE RESERVATION              ║");
        System.out.println("╚════════════════════════════════════════╝");

        // Use logged-in user's name automatically
        String name = currentUserName != null ? currentUserName : "Guest";
        
        if (currentUserName == null) {
            System.out.println("Warning: No user logged in. Using 'Guest'");
        } else {
            System.out.println("Logged-in User: " + name);
        }

        System.out.print("\nRoom/Space Name: ");
        String room = sc.nextLine().trim();

        System.out.print("Schedule (e.g., 10AM-12PM): ");
        String schedule = sc.nextLine().trim();

        // Check if room is available for the specified time
        if (!isRoomAvailable(room, schedule)) {
            System.out.println("\n✗ This room is already reserved for that time slot.");
            System.out.println("Please check availability and choose another time.");
            return;
        }

        // Fixed room rate of $8/hour
        double roomRate = 8.0;
        
        // Automatically calculate duration from schedule
        int duration = calculateDurationFromSchedule(schedule);
        
        if (duration <= 0) {
            System.out.println("Invalid schedule format. Use format like '10AM-12PM'");
            return;
        }

        System.out.println("\n--- Reservation Details ---");
        System.out.println("Room Rate: $" + roomRate + "/hour (Fixed)");
        System.out.println("Duration: " + duration + " hours (Auto-calculated)");
        System.out.println("Total Cost: $" + String.format("%.2f", roomRate * duration));

        // Create payment processor for this reservation
        RoomReservationPayment payment = new RoomReservationPayment(roomRate, duration, room);
        
        // Process payment
        payment.processReservationPayment(100000); // Assuming customer has sufficient balance
        
        // Create reservation only if payment is successful
        RoomReservation newRes = new RoomReservation(nextId++, name, room, schedule, 
                                                      payment.getTransactionID(), 
                                                      payment.getFinalAmount());
        reservations.add(newRes);

        System.out.println("\n✓ Reservation successful!");
        System.out.println("Reservation ID: " + newRes.getId());
        System.out.println("Occupant: " + name);
        System.out.println("Room: " + room);
        System.out.println("Time: " + schedule);
        System.out.println("Transaction ID: " + payment.getTransactionID());
        System.out.println("Total Cost: $" + String.format("%.2f", payment.getFinalAmount()));
    }

    // FEATURE 2: SCHEDULING & AVAILABILITY
    static void checkAvailability(Scanner sc) {

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   ROOM SCHEDULE & AVAILABILITY          ║");
        System.out.println("╚════════════════════════════════════════╝");

        System.out.print("Enter Room/Space Name: ");
        String room = sc.nextLine().trim();

        boolean found = false;

        for (Reservation r : reservations) {
            if (r.getRoom().equalsIgnoreCase(room)) {
                System.out.println("\n--- SCHEDULED TIMES ---");
                display(r);
                found = true;
            }
        }

        if (!found) {
            System.out.println("\n✓ Room '" + room + "' is fully available.");
        }
    }

    static void viewAll() {

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      ALL RESERVATIONS                   ║");
        System.out.println("╚════════════════════════════════════════╝");

        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        for (Reservation r : reservations) {
            display(r);
        }
    }

    static void display(Reservation r) {
        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("Reservation ID : " + r.getId());
        System.out.println("Occupant Name  : " + r.getName());
        System.out.println("Room/Space     : " + r.getRoom());
        System.out.println("Schedule       : " + r.getSchedule());
        System.out.println("Status         : " + r.getDetails());
        if (r instanceof RoomReservation) {
            RoomReservation rr = (RoomReservation) r;
            System.out.println("Transaction ID : " + rr.getTransactionID());
            System.out.println("Payment Amount : $" + String.format("%.2f", rr.getPaymentAmount()));
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // SUPERCLASS (ABSTRACTION)
    static abstract class Reservation {

        protected int id;
        protected String name;
        protected String room;
        protected String schedule;

        public Reservation(int id, String name, String room, String schedule) {
            this.id = id;
            this.name = name;
            this.room = room;
            this.schedule = schedule;
        }

        public abstract String getDetails();

        public int getId() { return id; }
        public String getName() { return name; }
        public String getRoom() { return room; }
        public String getSchedule() { return schedule; }
    }

    // SUBCLASS (INHERITANCE + OVERRIDING)
    // Now enhanced with payment information
    static class RoomReservation extends Reservation {

        private String status;
        private String transactionID;
        private double paymentAmount;

        public RoomReservation(int id, String name, String room, String schedule, 
                             String transactionID, double paymentAmount) {
            super(id, name, room, schedule);
            this.status = "Reserved & Paid";
            this.transactionID = transactionID;
            this.paymentAmount = paymentAmount;
        }

        @Override
        public String getDetails() {
            return status;
        }

        public String getTransactionID() {
            return transactionID;
        }

        public double getPaymentAmount() {
            return paymentAmount;
        }
    }
}