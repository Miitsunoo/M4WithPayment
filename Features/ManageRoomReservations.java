package Features;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

public class ManageRoomReservations {

    // POLYMORPHISM: List of superclass
    static ArrayList<Reservation> reservations = new ArrayList<>();
    static int nextId = 1;
    static String loggedOccupantLastName = null;

    public static void setLoggedOccupantLastName(String lastName) {
        loggedOccupantLastName = lastName;
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

        String name;
        if (loggedOccupantLastName != null && !loggedOccupantLastName.isEmpty()) {
            name = loggedOccupantLastName;
            System.out.println("Occupant Name: " + name + " (from login last name)");
        } else {
            System.out.print("Occupant Name: ");
            name = sc.nextLine();
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

        LocalTime[] parsedSchedule = parseSchedule(schedule);
        if (parsedSchedule == null) {
            System.out.println("Invalid schedule format. Use examples like 10AM-12PM or 09:00-11:00.");
            return;
        }

        LocalTime startTime = parsedSchedule[0];
        LocalTime endTime = parsedSchedule[1];
        double durationHours = computeDurationHours(startTime, endTime);

        if (durationHours <= 0) {
            System.out.println("Invalid schedule. End time must be later than start time.");
            return;
        }

        for (Reservation r : reservations) {
            if (r.getRoom().equalsIgnoreCase(room) &&
                timesOverlap(r.getStartTime(), r.getEndTime(), startTime, endTime)) {

                System.out.println("This room is already reserved for that schedule or an overlapping time.");
                return;
            }
        }
        return true;
    }

    // FEATURE 1: ROOM & SPACE RESERVATION WITH PAYMENT INTEGRATION
    static void reserveRoom(Scanner sc) {

        RoomReservation newRes = new RoomReservation(nextId++, name, room, schedule, startTime, endTime);
        reservations.add(newRes);

        double roomRate = 8.0;
        RoomReservationPayment payment = new RoomReservationPayment(roomRate, durationHours, room);
        payment.processReservationPayment(payment.getFinalAmount());

        System.out.println("Reservation successful.");
        System.out.println("Total charged: $" + String.format("%.2f", payment.getFinalAmount()));
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

    static LocalTime[] parseSchedule(String schedule) {
        String[] parts = schedule.trim().split("\\s*[-–\\u2013\\u2014]\\s*");
        if (parts.length != 2) {
            return null;
        }

        LocalTime start = parseTime(parts[0]);
        LocalTime end = parseTime(parts[1]);
        if (start == null || end == null) {
            return null;
        }

        return new LocalTime[] { start, end };
    }

    static LocalTime parseTime(String timeText) {
        if (timeText == null) {
            return null;
        }

        String text = timeText.trim().toUpperCase().replaceAll("\\s+", "");

        String[] patterns = {
            "h:mma",
            "ha",
            "H:mm",
            "H"
        };

        for (String pattern : patterns) {
            try {
                return LocalTime.parse(text, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    static double computeDurationHours(LocalTime start, LocalTime end) {
        return Duration.between(start, end).toMinutes() / 60.0;
    }

    static boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    // SUPERCLASS (ABSTRACTION)
    static abstract class Reservation {

        protected int id;
        protected String name;
        protected String room;
        protected String schedule;
        protected LocalTime startTime;
        protected LocalTime endTime;

        public Reservation(int id, String name, String room, String schedule, LocalTime startTime, LocalTime endTime) {
            this.id = id;
            this.name = name;
            this.room = room;
            this.schedule = schedule;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public abstract String getDetails();

        public int getId() { return id; }
        public String getName() { return name; }
        public String getRoom() { return room; }
        public String getSchedule() { return schedule; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
    }

    // SUBCLASS (INHERITANCE + OVERRIDING)
    // Now enhanced with payment information
    static class RoomReservation extends Reservation {

        private String status;
        private String transactionID;
        private double paymentAmount;

        public RoomReservation(int id, String name, String room, String schedule, LocalTime startTime, LocalTime endTime) {
            super(id, name, room, schedule, startTime, endTime);
            this.status = "Reserved";
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