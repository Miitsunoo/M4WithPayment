package Features;

import java.util.Scanner;
import java.util.List;
import Models.Occupant;
import Models.Keycard;
import Repositories.OccupantRepo;
import Repositories.KeycardRepo;

/**
 * KeycardAccessFeatures - Main login system and feature access for the bunker system
 * Handles user authentication and routes to appropriate features based on access level
 */
public class KeycardAccessFeatures {

    private static Scanner scanner = new Scanner(System.in);

    /**
     * Main entry point - starts the login system
     */
    public static void main(String[] args) {
        displayWelcome();
        
        while (true) {
            if (login()) {
                // Login successful, logout handled from feature menu
            } else {
                System.out.print("\nTry again? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("yes") && !response.equals("y")) {
                    break;
                }
            }
        }
        
        System.out.println("\n===============================================");
        System.out.println("Thank you for using the Bunker System");
        System.out.println("===============================================\n");
        scanner.close();
    }

    /**
     * Display welcome banner
     */
    private static void displayWelcome() {
        System.out.println("\n===============================================");
        System.out.println("BUNKER SYSTEM - KEYCARD ACCESS CONTROL");
        System.out.println("LOGIN SYSTEM");
        System.out.println("===============================================\n");
    }

    /**
     * Login system - prompts for credentials and verifies against database
     * @return true if login successful, false otherwise
     */
    private static boolean login() {
        System.out.println("\n===== LOGIN =====");
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine().trim();
        
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine().trim();
        
        System.out.print("Enter Occupant ID: ");
        String idInput = scanner.nextLine().trim();
        
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
        
        // Verify occupant in database
        if (!OccupantRepo.verifyOccupant(firstName, lastName, occupantId)) {
            System.out.println("\nLOGIN FAILED: Credentials do not match any registered occupant.");
            return false;
        }
        
        // Get occupant details
        Occupant user = OccupantRepo.getOccupantById(occupantId);
        if (user == null) {
            System.out.println("\nLOGIN FAILED: Occupant not found.");
            return false;
        }
        
        // Get user's keycard
        List<Keycard> keycards = KeycardRepo.getKeycardsByOccupantId(occupantId);
        if (keycards.isEmpty()) {
            System.out.println("\nLOGIN FAILED: No keycard assigned to this occupant.");
            return false;
        }
        
        // Find first active keycard
        Keycard userCard = null;
        for (Keycard card : keycards) {
            if (card.isActive()) {
                userCard = card;
                break;
            }
        }
        
        if (userCard == null) {
            System.out.println("\nLOGIN FAILED: No active keycard found.");
            return false;
        }
        
        // Login successful
        System.out.println("\nLogin successful! Welcome, " + user.getFullName() + "!");
        
        // Display verification
        displayVerification(user, userCard);
        
        // Access feature menu
        accessFeatures(user, userCard);
        
        return true;
    }

    /**
     * Display verified user information
     */
    public static void displayVerification(Occupant user, Keycard card) {
        System.out.println("===============================================");
        System.out.println("USER VERIFICATION");
        System.out.println("===============================================");
        System.out.println("\nVerification Details:");
        System.out.println("  First Name    : " + user.getFirstName());
        System.out.println("  Last Name     : " + user.getLastName());
        System.out.println("  Occupant ID   : " + user.getOccupantId());
        System.out.println("\nKeycard Details:");
        System.out.println("  Keycard ID    : " + card.getKeycardId());
        System.out.println("  Keycard Code  : " + card.getKeycardCode());
        System.out.println("  Status        : " + (card.isActive() ? "ACTIVE" : "INACTIVE"));
        System.out.println("  Access Level  : " + getAccessLevelName(card.getAccessLevel()) + " (" + card.getAccessLevel() + ")");
        System.out.println("  Issued At     : " + card.getIssuedAt());
    }



    /**
     * Display available features and handle navigation
     */
    public static void accessFeatures(Occupant user, Keycard card) {

        while (true) {
System.out.println("\n===============================================");
        System.out.println("SYSTEM FEATURES MENU");
        System.out.println("===============================================");

            System.out.println("\nLogged in as: " + user.getFullName() + " (" + getAccessLevelName(card.getAccessLevel()) + " - Level " + card.getAccessLevel() + ")");
            System.out.println("\nAvailable Features:\n");

            System.out.println("[1] View Keycard Access Information");
            System.out.println("    View detailed keycard information");

            System.out.println("[2] Manage Occupant Profiles");
            System.out.println("    Requires Admin access (level 3)");

            System.out.println("[3] Assign Work Duties");
            System.out.println("    Requires Employee access (level 2)");

            System.out.println("[4] Manage Room Reservations");
            System.out.println("    Available to all access levels");

            System.out.println("\n[0] Logout");
            System.out.print("\nSelect feature (0-4): ");

            String choice = scanner.nextLine().trim();

            if (handleFeatureChoice(choice, user, card)) {
                break; // Return to login
            }
        }
    }



    /**
     * Handle feature selection
     * @param choice User's menu choice
     * @param user The logged-in occupant
     * @param card The occupant's keycard
     * @return true if user wants to logout and return to login, false otherwise
     */
    private static boolean handleFeatureChoice(String choice, Occupant user, Keycard card) {
        if (choice.equals("0")) {
            return true;
        }

        if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3") && !choice.equals("4")) {
            System.out.println("Invalid choice. Please try again.\n");
            return false;
        }

        if (!isAccessAllowed(choice, card.getAccessLevel())) {
            System.out.println("Authority level too low.");
            return false;
        }

        switch (choice) {
            case "1":
                displayKeycardAccessInfo(user, card);
                return false;

            case "2":
                ManageOccupantProfiles.main(new String[]{});
                return false;

            case "3":
                AssignWorkDuties.main(new String[]{});
                return false;

            case "4":
                ManageRoomReservations.main(new String[]{});
                return false;

            default:
                System.out.println("Invalid choice. Please try again.\n");
                return false;
        }
    }

    public static boolean isAccessAllowed(String choice, int accessLevel) {
        switch (choice) {
            case "1":
                return true;
            case "2":
                return accessLevel >= 3;
            case "3":
                return accessLevel >= 2;
            case "4":
                return accessLevel >= 1;
            default:
                return false;
        }
    }

    public static String getAccessLevelName(int accessLevel) {
        switch (accessLevel) {
            case 1:
                return "Resident";
            case 2:
                return "Employee";
            case 3:
                return "Admin";
            default:
                return "Unknown";
        }
    }

    /**
     * Display keycard access information
     */
    private static void displayKeycardAccessInfo(Occupant user, Keycard card) {
        System.out.println("\n===============================================");
        System.out.println("KEYCARD ACCESS INFORMATION");
        System.out.println("===============================================");
        
        System.out.println("\nYour Keycard Details:");
        System.out.println("  Keycard ID    : " + card.getKeycardId());
        System.out.println("  Keycard Code  : " + card.getKeycardCode());
        System.out.println("  Holder        : " + user.getFullName());
        System.out.println("  Occupant ID   : " + user.getOccupantId());
        System.out.println("  Status        : " + (card.isActive() ? "ACTIVE" : "INACTIVE"));
        System.out.println("  Access Level  : " + getAccessLevelName(card.getAccessLevel()) + " (" + card.getAccessLevel() + ")");
        System.out.println("  Issued At     : " + card.getIssuedAt());
        System.out.println();
        
        System.out.print("Press Enter to return to menu...");
        scanner.nextLine();
    }

    // ================================
    // TESTING MAIN METHODS
    // ================================

    /**
     * Main method for database testing
     * (From DatabaseTest.class functionality)
     */
    public static void mainDatabaseTest(String[] args) {
        System.out.println("===============================================");
        System.out.println("BUNKER SYSTEM - DATABASE TEST");
        System.out.println("===============================================\n");

        // Test database connection
        Database.DatabaseConnection.testConnection();

        // Test occupants
        Repositories.OccupantRepo.testDatabaseConnection();

        // Test keycards
        Repositories.KeycardRepo.testDatabaseConnection();

        System.out.println("Database testing complete.");
    }

    /**
     * Main method for assigning keycards to occupants
     * (From AssignKeycards.class functionality)
     */
    public static void mainAssignKeycards(String[] args) {
        System.out.println("===============================================");
        System.out.println("BUNKER SYSTEM - KEYCARD ASSIGNMENT");
        System.out.println("===============================================\n");

        int assigned = Repositories.KeycardRepo.assignKeycardsToOccupantsWithoutCards();

        System.out.println("\nKeycard assignment process complete.");
        System.out.println("Assigned " + assigned + " new keycards.");
    }

    /**
     * Main method for assigning random keycard levels
     * (From AssignRandomKeycards.class functionality)
     */
    public static void mainAssignRandomLevels(String[] args) {
        System.out.println("===============================================");
        System.out.println("BUNKER SYSTEM - RANDOM LEVEL ASSIGNMENT");
        System.out.println("===============================================\n");

        int updated = Repositories.KeycardRepo.assignRandomKeycardLevels();

        System.out.println("\nRandom level assignment complete.");
        System.out.println("Updated " + updated + " keycards with random access levels.");
    }

    /**
     * Main method for registration testing
     * (From RegistrationTest.class functionality)
     */
    public static void mainRegistrationTest(String[] args) {
        System.out.println("===============================================");
        System.out.println("BUNKER SYSTEM - REGISTRATION TEST");
        System.out.println("===============================================\n");

        boolean success = Repositories.OccupantRepo.testRegistrationProcess();

        System.out.println("\nRegistration testing complete.");
        System.out.println("Test result: " + (success ? "PASSED" : "FAILED"));
    }

    /**
     * Main method for schema testing
     * (From SchemaTest.class functionality)
     */
    public static void mainSchemaTest(String[] args) {
        System.out.println("===============================================");
        System.out.println("BUNKER SYSTEM - SCHEMA TEST");
        System.out.println("===============================================\n");

        Database.DatabaseConnection.testDatabaseSchema();

        System.out.println("Schema testing complete.");
    }

    /**
     * Main method for occupant testing and summary
     * (From TestOccupants.class functionality)
     */
    public static void mainOccupantTest(String[] args) {
        System.out.println("===============================================");
        System.out.println("BUNKER SYSTEM - OCCUPANT TEST");
        System.out.println("===============================================\n");

        Repositories.OccupantRepo.displayOccupantSummary();

        System.out.println("Occupant testing complete.");
    }
}
