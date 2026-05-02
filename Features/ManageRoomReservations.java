package Features;

import java.util.ArrayList;
import java.util.Scanner;

public class ManageRoomReservations {

    // POLYMORPHISM: List of superclass
    static ArrayList<Reservation> reservations = new ArrayList<>();
    static int nextId = 1;

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

    // FEATURE 1: ROOM & SPACE RESERVATION
    static void reserveRoom(Scanner sc) {

        System.out.println("\n--- ROOM / SPACE RESERVATION ---");

        System.out.print("Occupant Name: ");
        String name = sc.nextLine();

        System.out.print("Room/Space Name: ");
        String room = sc.nextLine();

        System.out.print("Schedule (e.g., 10AM-12PM): ");
        String schedule = sc.nextLine();

        // Check if already reserved
        for (Reservation r : reservations) {
            if (r.getRoom().equalsIgnoreCase(room) &&
                r.getSchedule().equalsIgnoreCase(schedule)) {

                System.out.println("This room is already reserved for that schedule.");
                return;
            }
        }

        RoomReservation newRes = new RoomReservation(nextId++, name, room, schedule);
        reservations.add(newRes);

        System.out.println("Reservation successful.");
    }

    // FEATURE 2: SCHEDULING & AVAILABILITY
    static void checkAvailability(Scanner sc) {

        System.out.print("\nEnter Room/Space Name: ");
        String room = sc.nextLine();

        boolean found = false;

        for (Reservation r : reservations) {
            if (r.getRoom().equalsIgnoreCase(room)) {
                System.out.println("\n--- SCHEDULED ---");
                display(r);
                found = true;
            }
        }

        if (!found) {
            System.out.println("Room is fully available.");
        }
    }

    static void viewAll() {

        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        for (Reservation r : reservations) {
            display(r);
        }
    }

    static void display(Reservation r) {
        System.out.println("\n-------------------------------");
        System.out.println("Reservation ID : " + r.getId());
        System.out.println("Occupant Name  : " + r.getName());
        System.out.println("Room/Space     : " + r.getRoom());
        System.out.println("Schedule       : " + r.getSchedule());
        System.out.println("Status         : " + r.getDetails());
        System.out.println("-------------------------------");
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
    static class RoomReservation extends Reservation {

        private String status;

        public RoomReservation(int id, String name, String room, String schedule) {
            super(id, name, room, schedule);
            this.status = "Reserved";
        }

        @Override
        public String getDetails() {
            return status;
        }
    }
}
