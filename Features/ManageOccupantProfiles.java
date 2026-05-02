package Features;

import java.util.Scanner;
import java.util.List;
import Models.Occupant;
import Models.Keycard;
import Repositories.OccupantRepo;
import Repositories.KeycardRepo;

public class ManageOccupantProfiles {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("\n======================================");
            System.out.println("   UNDERGROUND BUNKER MANAGEMENT");
            System.out.println("======================================");
            System.out.println("[1] Register Bunker Occupant");
            System.out.println("[2] View Occupant Profile");
            System.out.println("[3] Edit Occupant Profile");
            System.out.println("[4] View All Occupants");
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
                case 1: register(sc); break;
                case 2: viewProfile(sc); break;
                case 3: editProfile(sc); break;
                case 4: viewAll(); break;
                case 5: System.out.println("Exiting Bunker System..."); break;
                default: System.out.println("Invalid choice.");
            }

        } while (choice != 5);
    }

    // ================================
    // FEATURE 1: REGISTER OCCUPANT
    // ================================
    static void register(Scanner sc) {

        System.out.println("\n--- BUNKER OCCUPANT REGISTRATION ---");

        System.out.print("First Name: ");
        String firstName = sc.nextLine().trim();

        System.out.print("Last Name: ");
        String lastName = sc.nextLine().trim();

        System.out.print("Email: ");
        String email = sc.nextLine().trim();

        System.out.print("Phone: ");
        String phone = sc.nextLine().trim();

        // Validate input
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            System.out.println("ERROR: First name, last name, and email are required.");
            return;
        }

        // Create occupant in database
        int occupantId = OccupantRepo.createOccupant(firstName, lastName, email, phone);
        if (occupantId != -1) {
            // Choose occupant status
            System.out.println("\nSelect occupant status:");
            System.out.println("[1] Resident");
            System.out.println("[2] Employee");
            System.out.println("[3] Admin");
            System.out.print("Enter status (1-3) [default Resident]: ");

            int accessLevel = 1;
            String accessLevelInput = sc.nextLine().trim();
            if (!accessLevelInput.isEmpty()) {
                try {
                    accessLevel = Integer.parseInt(accessLevelInput);
                    if (accessLevel < 1 || accessLevel > 3) {
                        System.out.println("Invalid level selected. Defaulting to Resident.");
                        accessLevel = 1;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Defaulting to Resident.");
                    accessLevel = 1;
                }
            }

            // Generate unique keycard code
            String keycardCode = "KC-" + String.format("%06d", occupantId);
            String accessLevelName = KeycardAccessFeatures.getAccessLevelName(accessLevel);

            // Create keycard with the chosen access level
            if (KeycardRepo.createKeycard(occupantId, keycardCode, accessLevel)) {
                System.out.println("Occupant successfully registered into the bunker!");
                System.out.println("Keycard assigned: " + keycardCode + " (Status: " + accessLevelName + ")");
            } else {
                System.out.println("Occupant registered, but keycard assignment failed.");
            }
        } else {
            System.out.println("ERROR: Failed to register occupant.");
        }
    }

    // ================================
    // FEATURE 2: VIEW PROFILE
    // ================================
    static void viewProfile(Scanner sc) {

        System.out.print("\nEnter Occupant ID: ");
        int id = sc.nextInt(); sc.nextLine();

        Occupant occupant = OccupantRepo.getOccupantById(id);
        if (occupant != null) {
            display(occupant);
        } else {
            System.out.println("Occupant not found.");
        }
    }

    // ================================
    // FEATURE 3: EDIT PROFILE
    // ================================
    static void editProfile(Scanner sc) {

        System.out.print("\nEnter Occupant ID: ");
        int id = sc.nextInt(); sc.nextLine();

        Occupant occupant = OccupantRepo.getOccupantById(id);
        if (occupant == null) {
            System.out.println("Occupant not found.");
            return;
        }

        System.out.println("Current details:");
        display(occupant);

        String status = getOccupantStatus(id);
        System.out.println("Status      : " + status);

        System.out.println("\nWhat would you like to do?");
        System.out.println("[1] Edit occupant profile");
        System.out.println("[2] Delete occupant profile");
        System.out.println("[0] Cancel");
        System.out.print("Select option: ");

        int action = 0;
        try {
            action = sc.nextInt(); sc.nextLine();
        } catch (Exception e) {
            sc.nextLine();
            System.out.println("Invalid option.");
            return;
        }

        switch (action) {
            case 0:
                System.out.println("Cancelled.");
                return;
            case 2:
                System.out.print("Are you sure you want to delete this profile? (yes/no): ");
                String confirm = sc.nextLine().trim().toLowerCase();
                if (confirm.equals("yes") || confirm.equals("y")) {
                    KeycardRepo.deleteKeycardsByOccupantId(id);
                    if (OccupantRepo.deleteOccupant(id)) {
                        System.out.println("Occupant profile deleted successfully.");
                    } else {
                        System.out.println("ERROR: Failed to delete occupant profile.");
                    }
                } else {
                    System.out.println("Deletion cancelled.");
                }
                return;
            case 1:
                break;
            default:
                System.out.println("Invalid option.");
                return;
        }

        System.out.print("New First Name (leave empty to keep current): ");
        String firstName = sc.nextLine().trim();
        if (firstName.isEmpty()) firstName = occupant.getFirstName();

        System.out.print("New Last Name (leave empty to keep current): ");
        String lastName = sc.nextLine().trim();
        if (lastName.isEmpty()) lastName = occupant.getLastName();

        System.out.print("New Email (leave empty to keep current): ");
        String email = sc.nextLine().trim();
        if (email.isEmpty()) email = occupant.getEmail();

        System.out.print("New Phone (leave empty to keep current): ");
        String phone = sc.nextLine().trim();
        if (phone.isEmpty()) phone = occupant.getPhone();

        if (OccupantRepo.updateOccupant(id, firstName, lastName, email, phone)) {
            System.out.print("Update keycard status too? (yes/no): ");
            String changeStatus = sc.nextLine().trim().toLowerCase();
            if (changeStatus.equals("yes") || changeStatus.equals("y")) {
                List<Keycard> keycards = KeycardRepo.getKeycardsByOccupantId(id);
                if (!keycards.isEmpty()) {
                    Keycard activeKeycard = null;
                    for (Keycard keycard : keycards) {
                        if (keycard.isActive()) {
                            activeKeycard = keycard;
                            break;
                        }
                    }
                    if (activeKeycard == null) {
                        activeKeycard = keycards.get(0);
                    }

                    System.out.println("Select new status:");
                    System.out.println("[1] Resident");
                    System.out.println("[2] Employee");
                    System.out.println("[3] Admin");
                    System.out.print("Enter level (1-3): ");
                    int newLevel = activeKeycard.getAccessLevel();
                    try {
                        newLevel = Integer.parseInt(sc.nextLine().trim());
                        if (newLevel < 1 || newLevel > 3) {
                            System.out.println("Invalid access level. Keeping current status.");
                            newLevel = activeKeycard.getAccessLevel();
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Keeping current status.");
                        newLevel = activeKeycard.getAccessLevel();
                    }

                    if (KeycardRepo.updateKeycardAccessLevel(activeKeycard.getKeycardId(), newLevel)) {
                        System.out.println("Profile and status updated successfully!");
                    } else {
                        System.out.println("Profile updated, but failed to update keycard status.");
                    }
                } else {
                    System.out.println("Profile updated, but no keycard found to update status.");
                }
            } else {
                System.out.println("Profile updated successfully!");
            }
        } else {
            System.out.println("ERROR: Failed to update profile.");
        }
    }

    // ================================
    // FEATURE 4: VIEW ALL OCCUPANTS
    // ================================
    static void viewAll() {

        List<Occupant> occupants = OccupantRepo.getAllOccupants();
        if (occupants.isEmpty()) {
            System.out.println("No occupants in bunker.");
            return;
        }

        for (Occupant occupant : occupants) {
            display(occupant);
        }
    }

    // ================================
    // DISPLAY
    // ================================
    static void display(Occupant occupant) {
        System.out.println("\n-------------------------------");
        System.out.println("Occupant ID : " + occupant.getOccupantId());
        System.out.println("Name        : " + occupant.getFirstName() + " " + occupant.getLastName());
        System.out.println("Email       : " + occupant.getEmail());
        System.out.println("Phone       : " + occupant.getPhone());
        System.out.println("Status      : " + getOccupantStatus(occupant.getOccupantId()));
        System.out.println("Registered  : " + occupant.getRegisteredAt());
        System.out.println("-------------------------------");
    }

    static String getOccupantStatus(int occupantId) {
        List<Keycard> keycards = KeycardRepo.getKeycardsByOccupantId(occupantId);
        if (keycards.isEmpty()) {
            return "Unassigned";
        }

        Keycard active = null;
        for (Keycard keycard : keycards) {
            if (keycard.isActive()) {
                active = keycard;
                break;
            }
        }
        if (active == null) {
            active = keycards.get(0);
        }

        return KeycardAccessFeatures.getAccessLevelName(active.getAccessLevel());
    }
}

