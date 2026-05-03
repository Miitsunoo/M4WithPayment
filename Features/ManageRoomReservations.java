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
    static String currentUserName = null;

    public static void setLoggedOccupantLastName(String lastName) {
        loggedOccupantLastName = lastName;
    }

    public static void setCurrentUser(String name) {
        currentUserName = name;
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
            System.out.println("[4] View Income Statement");
            System.out.println("[5] Exit");
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
                case 4: RoomReservationPayment.displayAllTransactions(); break;
                case 5: System.out.println("Exiting system..."); break;
                default: System.out.println("Invalid choice.");
            }

        } while (choice != 5);
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
        LocalTime[] parsedSchedule = parseSchedule(schedule);
        if (parsedSchedule == null) {
            System.out.println("Invalid schedule format. Use examples like 10AM-12PM or 09:00-11:00.");
            return false;
        }

        LocalTime startTime = parsedSchedule[0];
        LocalTime endTime = parsedSchedule[1];
        double durationHours = computeDurationHours(startTime, endTime);

        if (durationHours <= 0) {
            System.out.println("Invalid schedule. End time must be later than start time.");
            return false;
        }

        for (Reservation r : reservations) {
            if (r.getRoom().equalsIgnoreCase(room) &&
                timesOverlap(r.getStartTime(), r.getEndTime(), startTime, endTime)) {

                System.out.println("This room is already reserved for that schedule or an overlapping time.");
                return false;
            }
        }
        return true;
    }

    /**
     * Validate if a room number is within the valid range (101-110, 201-210, 301-310)
     */
    static boolean isValidRoomNumber(String room) {
        try {
            int roomNum = Integer.parseInt(room);
            // Check if room is in valid ranges: 101-110, 201-210, 301-310
            return (roomNum >= 101 && roomNum <= 110) ||
                   (roomNum >= 201 && roomNum <= 210) ||
                   (roomNum >= 301 && roomNum <= 310);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // FEATURE 1: ROOM & SPACE RESERVATION WITH PAYMENT INTEGRATION
    static void reserveRoom(Scanner sc) {
        String name;
        if (currentUserName != null && !currentUserName.isEmpty()) {
            name = currentUserName;
            System.out.println("Occupant Name: " + name);
        } else if (loggedOccupantLastName != null && !loggedOccupantLastName.isEmpty()) {
            name = loggedOccupantLastName;
            System.out.println("Occupant Name: " + name);
        } else {
            System.out.print("Occupant Name: ");
            name = sc.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Occupant name cannot be empty.");
                return;
            }
        }

        System.out.print("Enter Room Number (101-110, 201-210, 301-310): ");
        String room = sc.nextLine().trim();
        if (room.isEmpty()) {
            System.out.println("Room number cannot be empty.");
            return;
        }

        // Validate room number format
        if (!isValidRoomNumber(room)) {
            System.out.println("Invalid room number. Please enter a valid room (101-110, 201-210, or 301-310).");
            return;
        }

        System.out.print("Enter Schedule (e.g. 10AM-12PM or 09:00-11:00): ");
        String schedule = sc.nextLine().trim();
        LocalTime[] parsedSchedule = parseSchedule(schedule);
        if (parsedSchedule == null) {
            System.out.println("Invalid schedule format. Use examples like 10AM-12PM or 09:00-11:00.");
            return;
        }

        if (!isRoomAvailable(room, schedule)) {
            return;
        }

        LocalTime startTime = parsedSchedule[0];
        LocalTime endTime = parsedSchedule[1];
        double scheduleDurationHours = computeDurationHours(startTime, endTime);
        if (scheduleDurationHours <= 0) {
            System.out.println("Invalid schedule. End time must be later than start time.");
            return;
        }

        System.out.print("Enter Room Rate ($): ");
        double roomRate;
        try {
            roomRate = Double.parseDouble(sc.nextLine().trim());
            if (roomRate <= 0) {
                System.out.println("Invalid room rate. Must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid room rate. Please enter a valid number.");
            return;
        }

        System.out.print("Enter Duration in hours: ");
        int durationHours;
        try {
            durationHours = Integer.parseInt(sc.nextLine().trim());
            if (durationHours <= 0) {
                System.out.println("Invalid duration. Must be a positive integer.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid duration. Please enter a valid integer.");
            return;
        }

        System.out.print("Enter discount amount (0 for none): ");
        double discountAmount;
        try {
            discountAmount = Double.parseDouble(sc.nextLine().trim());
            if (discountAmount < 0) {
                System.out.println("Invalid discount amount. Must be zero or positive.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid discount amount. Please enter a valid number.");
            return;
        }

        RoomReservationPayment payment = new RoomReservationPayment(roomRate, durationHours, room);
        if (discountAmount > 0) {
            payment.applyReservationDiscount(discountAmount);
        }

        double balance = payment.getFinalAmount();
        payment.processReservationPayment(balance);

        RoomReservation newRes = new RoomReservation(nextId++, name, room, schedule, startTime, endTime, payment.getTransactionID(), balance);
        reservations.add(newRes);

        System.out.println("Reservation successful.");
        System.out.println("Total charged: $" + String.format("%.2f", balance));
    }

    // FEATURE 2: SCHEDULING & AVAILABILITY - FLOOR-BASED ROOM INVENTORY
    static void checkAvailability(Scanner sc) {
        int currentFloor = 1;  // Start at Floor 1
        int choice = 0;

        while (true) {
            displayFloorRooms(currentFloor);

            System.out.print("\nSelect Option: ");
            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                sc.nextLine();
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:  // Next Floor
                    if (currentFloor < 3) {
                        currentFloor++;
                    } else {
                        System.out.println("You are already on the last floor.");
                    }
                    break;
                case 2:  // Previous Floor (only shown if available)
                    if (currentFloor > 1) {
                        currentFloor--;
                    } else {
                        System.out.println("You are already on the first floor.");
                    }
                    break;
                case 0:  // Exit
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    /**
     * Display all rooms on a specific floor with their status
     */
    static void displayFloorRooms(int floor) {
        System.out.println("\n========================================");
        System.out.println("Floor " + floor);
        System.out.println("========================================\n");

        int startRoom = floor * 100 + 1;
        int endRoom = startRoom + 10;

        for (int roomNum = startRoom; roomNum < endRoom; roomNum++) {
            String roomName = String.valueOf(roomNum);
            String status = getRoomStatus(roomName);
            System.out.println(roomNum + " - " + status);
        }

        System.out.println("\n========================================");
        if (floor < 3) {
            System.out.println("(1) Next Floor");
        }
        if (floor > 1) {
            System.out.println("(2) Previous Floor");
        }
        System.out.println("(0) Exit");
    }

    /**
     * Get the status of a specific room (Available or Reserved with time)
     */
    static String getRoomStatus(String roomNumber) {
        for (Reservation r : reservations) {
            if (r.getRoom().equals(roomNumber)) {
                return "Reserved (" + r.getSchedule() + ")";
            }
        }
        return "Available";
    }

    static void viewAll() {

        System.out.println("\n========================================");
        System.out.println("ALL RESERVATIONS");
        System.out.println("========================================");

        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        for (Reservation r : reservations) {
            display(r);
        }
    }

    static void display(Reservation r) {
        System.out.println("\n---------------------------------------");
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
        System.out.println("---------------------------------------");
    }

    static LocalTime[] parseSchedule(String schedule) {
        String[] parts = schedule.trim().split("\\s*-\\s*");
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

        public RoomReservation(int id, String name, String room, String schedule, LocalTime startTime, LocalTime endTime, String transactionID, double paymentAmount) {
            super(id, name, room, schedule, startTime, endTime);
            this.status = "Reserved";
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