package Features;

import java.util.Scanner;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import Models.WorkDuties;
import Models.Occupant;
import Repositories.WorkDutiesRepo;
import Repositories.OccupantRepo;

/**
 * AssignWorkDuties - Feature to manage work duty assignments
 */
public class AssignWorkDuties {

    private static Scanner scanner = new Scanner(System.in);
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void runMenu() {
        int choice = 0;

        do {
            System.out.println("\n======================================");
            System.out.println("   WORK DUTIES MANAGEMENT");
            System.out.println("======================================");
            System.out.println("[1] Assign Work Duty");
            System.out.println("[2] View Duties by Occupant");
            System.out.println("[3] View All Assigned Duties");
            System.out.println("[4] Update Duty Status");
            System.out.println("[5] Remove Work Duty");
            System.out.println("[6] Exit");
            System.out.print("Select Option: ");

            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                scanner.nextLine();
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    assignDuty();
                    break;
                case 2:
                    viewDutiesByOccupant();
                    break;
                case 3:
                    viewAllDuties();
                    break;
                case 4:
                    updateDutyStatus();
                    break;
                case 5:
                    removeDuty();
                    break;
                case 6:
                    System.out.println("Returning to main menu...");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 6);
    }

    public static void main(String[] args) {
        runMenu();
    }

    // ================================
    // FEATURE 1: ASSIGN WORK DUTY
    // ================================
    static void assignDuty() {
        System.out.println("\n--- ASSIGN WORK DUTY ---");

        System.out.print("Occupant ID: ");
        int occupantId = scanner.nextInt();
        scanner.nextLine();

        // Verify occupant exists
        Occupant occupant = OccupantRepo.getOccupantById(occupantId);
        if (occupant == null) {
            System.out.println("ERROR: Occupant not found.");
            return;
        }

        System.out.print("Duty Name: ");
        String dutyName = scanner.nextLine().trim();

        System.out.print("Assigned Date (yyyy-MM-dd): ");
        String dateInput = scanner.nextLine().trim();
        LocalDate assignedDate;
        try {
            assignedDate = LocalDate.parse(dateInput, dateFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("ERROR: Invalid date format. Use yyyy-MM-dd");
            return;
        }

        System.out.println("Shift Options: Morning, Afternoon, Night");
        System.out.print("Select Shift: ");
        String shift = scanner.nextLine().trim();

        // Validate shift
        if (!shift.equals("Morning") && !shift.equals("Afternoon") && !shift.equals("Night")) {
            System.out.println("ERROR: Invalid shift. Please choose Morning, Afternoon, or Night.");
            return;
        }

        // Validate input
        if (dutyName.isEmpty()) {
            System.out.println("ERROR: Duty name is required.");
            return;
        }

        // Create work duty
        WorkDuties duty = new WorkDuties(occupantId, dutyName, assignedDate, shift, "Pending");

        if (WorkDutiesRepo.assignDuty(duty)) {
            System.out.println("Work duty successfully assigned to " + occupant.getFirstName() + " " + occupant.getLastName() + "!");
        } else {
            System.out.println("ERROR: Failed to assign work duty.");
        }
    }

    // ================================
    // FEATURE 2: VIEW DUTIES BY OCCUPANT
    // ================================
    static void viewDutiesByOccupant() {
        System.out.print("\nEnter Occupant ID: ");
        int occupantId = scanner.nextInt();
        scanner.nextLine();

        Occupant occupant = OccupantRepo.getOccupantById(occupantId);
        if (occupant == null) {
            System.out.println("Occupant not found.");
            return;
        }

        List<WorkDuties> duties = WorkDutiesRepo.getDutiesByOccupantId(occupantId);

        if (duties.isEmpty()) {
            System.out.println("\nNo duties assigned to " + occupant.getFirstName() + " " + occupant.getLastName() + ".");
            return;
        }

        System.out.println("\n--- WORK DUTIES FOR " + occupant.getFirstName().toUpperCase() + " " + occupant.getLastName().toUpperCase() + " ---");
        displayDutiesList(duties);
    }

    // ================================
    // FEATURE 3: VIEW ALL DUTIES
    // ================================
    static void viewAllDuties() {
        List<WorkDuties> allDuties = WorkDutiesRepo.getAllDuties();

        if (allDuties.isEmpty()) {
            System.out.println("\nNo work duties assigned yet.");
            return;
        }

        System.out.println("\n--- ALL ASSIGNED WORK DUTIES ---");
        displayDutiesList(allDuties);
    }

    // ================================
    // FEATURE 4: UPDATE DUTY STATUS
    // ================================
    static void updateDutyStatus() {
        System.out.print("\nEnter Duty ID: ");
        int dutyId = scanner.nextInt();
        scanner.nextLine();

        WorkDuties duty = WorkDutiesRepo.getDutyById(dutyId);
        if (duty == null) {
            System.out.println("Duty not found.");
            return;
        }

        System.out.println("Current Status: " + duty.getStatus());
        System.out.println("Status Options: Pending, Ongoing, Completed");
        System.out.print("Enter New Status: ");
        String newStatus = scanner.nextLine().trim();

        // Validate status
        if (!newStatus.equals("Pending") && !newStatus.equals("Ongoing") && !newStatus.equals("Completed")) {
            System.out.println("ERROR: Invalid status. Please choose Pending, Ongoing, or Completed.");
            return;
        }

        duty.setStatus(newStatus);

        if (WorkDutiesRepo.updateDuty(duty)) {
            System.out.println("Duty status successfully updated to: " + newStatus);
        } else {
            System.out.println("ERROR: Failed to update duty status.");
        }
    }

    // ================================
    // FEATURE 5: REMOVE WORK DUTY
    // ================================
    static void removeDuty() {
        System.out.print("\nEnter Duty ID to Remove: ");
        int dutyId = scanner.nextInt();
        scanner.nextLine();

        WorkDuties duty = WorkDutiesRepo.getDutyById(dutyId);
        if (duty == null) {
            System.out.println("Duty not found.");
            return;
        }

        System.out.print("Are you sure you want to remove this duty? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            if (WorkDutiesRepo.removeDuty(dutyId)) {
                System.out.println("Work duty successfully removed.");
            } else {
                System.out.println("ERROR: Failed to remove work duty.");
            }
        } else {
            System.out.println("Operation cancelled.");
        }
    }

    // ================================
    // HELPER METHOD: DISPLAY DUTIES
    // ================================
    static void displayDutiesList(List<WorkDuties> duties) {
        for (WorkDuties duty : duties) {
            Occupant occupant = OccupantRepo.getOccupantById(duty.getOccupantId());
            String occupantName = (occupant != null) ? occupant.getFirstName() + " " + occupant.getLastName() : "Unknown";

            System.out.println("\nDuty ID: " + duty.getDutyId());
            System.out.println("   Occupant: " + occupantName + " (ID: " + duty.getOccupantId() + ")");
            System.out.println("   Duty Name: " + duty.getDutyName());
            System.out.println("   Assigned Date: " + duty.getAssignedDate());
            System.out.println("   Shift: " + duty.getShift());
            System.out.println("   Status: " + duty.getStatus());
        }
        System.out.println();
    }
}
