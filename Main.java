import Features.AssignWorkDuties;
import Features.KeycardAccessFeatures;
import Features.ManageOccupantProfiles;
import Features.ManageRoomReservations;
import Models.Keycard;
import Models.Occupant;
import java.util.Scanner;

public class Main {
    private static Scanner sc = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static Occupant currentUser = null;
    private static Keycard currentKeycard = null;

    public static void main(String[] args) {
        displayWelcome();

        // Require login first
        if (!performLogin()) {
            System.out.println("\n===============================================");
            System.out.println("Access Denied. System terminating.");
            System.out.println("===============================================\n");
            return;
        }

        // Show main menu after successful login
        displayMainMenu();
    }

    private static void displayWelcome() {
        System.out.println("\n===============================================");
        System.out.println("BUNKER MANAGEMENT SYSTEM");
        System.out.println("Secure Access Control System");
        System.out.println("===============================================\n");
    }

    private static boolean performLogin() {
        System.out.println("Authentication Required");
        System.out.println("Please log in to access the management system.\n");

        // Use the existing login system from KeycardAccessFeatures
        // We'll simulate the login process here to get user credentials

        System.out.println("===== LOGIN =====");
        System.out.print("Enter First Name: ");
        String firstName = sc.nextLine().trim();

        System.out.print("Enter Last Name: ");
        String lastName = sc.nextLine().trim();

        System.out.print("Enter Occupant ID: ");
        String idInput = sc.nextLine().trim();

        // Validate input
        if (firstName.isEmpty() || lastName.isEmpty() || idInput.isEmpty()) {
            System.out.println("\nERROR: All fields are required.");
            return false;
        }

        int occupantId;
        try {
            occupantId = Integer.parseInt(idInput);
        } catch (NumberFormatException e) {
            System.out.println("\nERROR: Invalid Occupant ID. Must be a number.");
            return false;
        }

        // Import necessary classes for verification
        try {
            // Verify occupant in database
            if (!Repositories.OccupantRepo.verifyOccupant(firstName, lastName, occupantId)) {
                System.out.println("\nLOGIN FAILED: Credentials do not match any registered occupant.");
                return false;
            }

            // Get occupant details
            currentUser = Repositories.OccupantRepo.getOccupantById(occupantId);
            if (currentUser == null) {
                System.out.println("\nLOGIN FAILED: Occupant not found.");
                return false;
            }

            // Get user's keycard
            java.util.List<Keycard> keycards = Repositories.KeycardRepo.getKeycardsByOccupantId(occupantId);
            if (keycards.isEmpty()) {
                System.out.println("\nLOGIN FAILED: No keycard assigned to this occupant.");
                return false;
            }

            // Find first active keycard
            for (Keycard card : keycards) {
                if (card.isActive()) {
                    currentKeycard = card;
                    break;
                }
            }

            if (currentKeycard == null) {
                System.out.println("\nLOGIN FAILED: No active keycard found.");
                return false;
            }

            // Login successful
            System.out.println("\nLogin successful! Welcome, " + currentUser.getFullName() + "!");
            KeycardAccessFeatures.displayVerification(currentUser, currentKeycard);
            isLoggedIn = true;
            return true;

        } catch (Exception e) {
            System.out.println("\nLOGIN ERROR: " + e.getMessage());
            return false;
        }
    }

    static void displayMainMenu() {
        int choice = 0;

        do {
            System.out.println("\n===============================================");
            System.out.println("BUNKER MANAGEMENT SYSTEM - MAIN MENU");
            System.out.println("===============================================\n");

            System.out.println("Logged in as: " + currentUser.getFullName() + " (ID: " + currentUser.getOccupantId() + ")");
            System.out.println("Access Level : " + KeycardAccessFeatures.getAccessLevelName(currentKeycard.getAccessLevel()) + " (Level " + currentKeycard.getAccessLevel() + ")");


            System.out.println("\n[1] Manage Occupant Profiles");
            System.out.println("    Register, view, and edit occupant information");

            System.out.println("[2] Assign Work Duties");
            System.out.println("    Create and manage occupant work assignments");

            System.out.println("[3] Manage Room Reservations");
            System.out.println("    Reserve and schedule bunker rooms/spaces");

            System.out.println("\n[0] Logout & Exit System");

            System.out.print("\nSelect an option (0-3): ");

            try {
                choice = sc.nextInt();
                sc.nextLine();
            } catch (Exception e) {
                sc.nextLine();
                System.out.println("\nInvalid input. Please enter a number between 0-3.");
                continue;
            }

            switch (choice) {
                case 1:
                    if (!KeycardAccessFeatures.isAccessAllowed("2", currentKeycard.getAccessLevel())) {
                        System.out.println("\nAuthority level too low.");
                        break;
                    }
                    System.out.println("\nLaunching Occupant Profile Manager...\n");
                    ManageOccupantProfiles.main(new String[]{});
                    break;

                case 2:
                    if (!KeycardAccessFeatures.isAccessAllowed("3", currentKeycard.getAccessLevel())) {
                        System.out.println("\nAuthority level too low.");
                        break;
                    }
                    System.out.println("\nLaunching Work Duties Manager...\n");
                    AssignWorkDuties.main(new String[]{});
                    break;

                case 3:
                    if (!KeycardAccessFeatures.isAccessAllowed("4", currentKeycard.getAccessLevel())) {
                        System.out.println("\nAuthority level too low.");
                        break;
                    }
                    System.out.println("\nLaunching Room Reservation System...\n");
                    ManageRoomReservations.setCurrentUser(currentUser.getFullName());
                    ManageRoomReservations.setLoggedOccupantLastName(currentUser.getLastName());
                    ManageRoomReservations.main(new String[]{});
                    break;

                case 0:
                    System.out.println("\n===============================================");
                    System.out.println("Thank you for using the Bunker System!");
                    System.out.println("Secure location access terminated.");
                    System.out.println("===============================================\n");
                    System.exit(0);
                    break;

                default:
                    System.out.println("\n Invalid choice. Please select an option between 0-3.");
            }

        } while (choice != 0);

        sc.close();
    }
}
